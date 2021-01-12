#include <string.h>
#include <iostream>
#include <pthread.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <fstream>
#include <unistd.h>
#include <vector>

#define SIZE_OF_QUEUE 5

//Maksymalna ilość użytkowników
#define MAX_NUMBER_OF_USERS 15

//Mutex dla użytkowników, zapobiega wpspółbieznej modyfikacji wektora users
pthread_mutex_t users_mutex = PTHREAD_MUTEX_INITIALIZER;

//Mutexy dla każdego pokoju, zapobiega sytuacji wspóbieznego pisania do pokoju rozmów
pthread_mutex_t rooms_mutex[6] = {PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER};


using namespace std;



//Struktura która zawiera informacje o kliencie: nazwe, deskrypor usera, numer pokoju
struct User {
    string nick;
    int descriptor;
    int room;
};

//struktura zawierająca dane które zostaną przekazane do wątku
struct thread_data_t {
    User user;
    vector<User>* users;
    pthread_mutex_t* users_mutex;
    pthread_mutex_t rooms_mutex[6];
    bool check;
}; 




//Funckja opróżnia bufor z śmieci pozostawionych po operacjach 
//Po operacji w buforze zostają dwa zbędne bajty
//Więc odczytujemy je aby wyczyścić bufor
void ThrowingRubbish(thread_data_t *th_data) {
    char buf[2];
    read(th_data -> user.descriptor, buf, sizeof(buf));
}

//Funkcja usuwajaca klienta o danym deskryptorze z wektora klientow
//Wywolywana po zakonczeniu uzytkowania z aplikacji przez klienta



//Funkcja opisująca działanie wątku - musi przyjmować argument typu (void *) i zwracać (void *)
//szablon funkcji z zajęc
void *ThreadBehavior(void *t_data){
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*) t_data;
    int character;

    char mode[5];
    
    while(1) {
        character = read(th_data -> user.descriptor, mode, sizeof(mode)); //szczytanie operacji do wykonania

        if(character == -1) {
            cout << "Błąd przy odczytywaniu - konczenie watku" << endl;
            break;
        }
        else if (character > 0) {
            //cout<<mode<<endl;
            //------------ login wszystkie operacje dotyczące logowania klienta-----
            if(strcmp( mode, "login") == 0) { 
                
                char length[2];
                int pom, read_result;
                int number = 0; //liczba odczytynych cyfr długości nicku
                string pom_string = "";
                int users_number =0;
                char* actualLength = new char[2];
                memset(actualLength, 0, 2);
                bool SameLoginExist = false;
                bool ReadError = false;
                bool MaxClients = false;
                

                //-------Sprawdzenie czy dozwolona liczba klientów nie została przekroczona
                pthread_mutex_lock(th_data->users_mutex);
                users_number = th_data -> users -> size();
                //cout<<"liczba użytkowników : "<<users_number<<endl;
                if(users_number == MAX_NUMBER_OF_USERS + 1){
                    MaxClients = true;
                }
                pthread_mutex_unlock(th_data->users_mutex);


                //---Sprawdzanie długości nicku klienta (nick może mieć długość od 1 do 99)
                while(number < 2) {
                    read_result = read(th_data -> user.descriptor, actualLength, 2 - number);
                    if(read_result == -1) {
                        cout << "EROR! Nie udało się odczytać długości nicku" << endl;
                        ReadError = true;
                    }
                    else if(read_result == 0) {
                        cout << "ERROR! Klient rozłączył się, podczas odczytywania długości nicku" << endl;
                        ReadError = true;
                    }
                    number += read_result;
                    pom_string = pom_string + actualLength;
                    if(number == 2) break;

                    //zerowanie aktualnej cyfry aby odczytać następną
                    delete actualLength; 
                    actualLength = new char[2-number];
                    memset(actualLength, 0, 2 - number);
                    }
                
                strcpy(length, pom_string.c_str());
                pom = atoi(length);
                char* name = new char[pom];
                memset(name, 0, pom);
                number = 0;
                pom_string = "";

                //------Odczytywanie nicku 
                while(number < pom) {
                    read_result = read(th_data -> user.descriptor, name, pom-number);
                    if(read_result == -1) {
                        cout << "ERROR! Błąd przy odczytywaniu nicku" << endl;
                        ReadError =  true;
                    }
                    else if(read_result == 0) {
                        cout << "ERROR! Klient rozłączył się, podczas odczytywania nicku"<< endl;
                        ReadError =  true;
                    }
                    number += read_result;
                    pom_string = pom_string + name;
                    if(number == pom) break;
                    delete name;
                    name = new char[pom-number];
                    memset(name, 0, pom-number);
                    }
    
                delete name; //na wszelki wypadek czasami zostawały jakieś śmieci w tej zmiennej

                char nick[pom_string.size()];
                strcpy(nick, pom_string.c_str());

                pthread_mutex_lock(th_data->users_mutex);
                int size = th_data -> users -> size();
                int user_index;

                //-------Sprawdzanie unikalności nicku klienta
                for(int i=0; i<size; i++) {
                if(!(*th_data->users)[i].nick.compare(nick)) {
                    cout << "Istnieje juz ktos o podanym nicku" << endl;
                    SameLoginExist =  true;
                    pthread_mutex_unlock(th_data->users_mutex);
                    }

                if((*th_data->users)[i].descriptor == th_data -> user.descriptor) user_index = i;
                }



                //Sprawdzamy czy zadane wymagania zostały spełnione i wysyłamy odpowiedni feedback do klienta
                if(SameLoginExist == false && ReadError == false && MaxClients == false) {
                    cout << "Dodano nick: " << nick << " dla klienta o deskryptorze " << th_data -> user.descriptor << endl;
                    th_data -> user.nick = nick;
                    th_data -> user.room = 0; //nadajemy numer poczekalni (respown)
                    (*th_data->users)[user_index].nick = nick; //ustawienie nicku danemu klientowi
                    pthread_mutex_unlock(th_data->users_mutex); //odblokowanie mutexa jeśli przeszedł test unikalności
                    
                    pthread_mutex_lock(&th_data->rooms_mutex[0]);//blokowanie poczekalni

                    //Wysłanie do klienta odpowiedz 0 = powodzenie
                    write(th_data -> user.descriptor, "0\n", 2 * sizeof(char)); 
                    pthread_mutex_unlock(&th_data->rooms_mutex[0]);
                }
                else if( MaxClients == true){
                    pthread_mutex_lock(&th_data->rooms_mutex[0]);
                    //Wysłanie do klienta odpowiedzi 1 = przekroczono max liczbe użytkowników 
                    write(th_data -> user.descriptor, "1\n", 2 * sizeof(char)); 
                    pthread_mutex_unlock(&th_data->rooms_mutex[0]);
                    break;} //lub zakonczenie watku
                
                else {
                    pthread_mutex_lock(&th_data->rooms_mutex[0]);
                    //Wysłanie do klienta odpowiedzi 2 = niepowodzenie 
                    write(th_data -> user.descriptor, "2\n", 2 * sizeof(char)); 
                    pthread_mutex_unlock(&th_data->rooms_mutex[0]);
                    break;} //lub zakonczenie watku

                ThrowingRubbish(th_data); // czyszczenie buffora
            }
        
        
        }
        memset(mode, 0, sizeof(mode));
    }


    cout << "Usunieto klienta o deskryptorze: " << th_data -> user.descriptor << endl;
    pthread_mutex_lock(th_data->users_mutex);
    pthread_mutex_lock(&th_data->rooms_mutex[th_data -> user.room]);
    //Usunięcie klienta z wektora użytkowników
    int size = 0;
    size = th_data -> users -> size();
    for(int i=0; i<size; i++) {
        if(th_data -> user.descriptor == (*th_data->users)[i].descriptor) {
            th_data->users->erase(th_data->users->begin() + i);
            break;
        }
    }
    pthread_mutex_unlock(&th_data->rooms_mutex[th_data -> user.room]);
    pthread_mutex_unlock(th_data->users_mutex);
    close(th_data -> user.descriptor);
    delete th_data; //czyszczenie struktury
    pthread_exit(NULL); //Zakonczenie watku
}

//Funkcja tworząca nowy wątek dla nowego połączenia klienta
//Szablon tej funkcji został zaczerpnięty z materiałów lab
void handleConnection( int connection_socket_descriptor, vector<User>* users, pthread_mutex_t* users_mutex, pthread_mutex_t* rooms_mutex){
    
    int create_result = 0; //Wynik funkcji tworzącej wątek


    pthread_t thread1; //uchwyt na wątek

    struct thread_data_t *t_data = new thread_data_t();

    bool descriptor_exist = false;
    pthread_mutex_lock(users_mutex); //blokujemy mutex klienta
    int users_size = users-> size(); //wielkość wektora klientów
    
    //Jeżeli w wketorze użytkowników dany deskryptor uzytkownika istnieje zmienną descriptor_exist ustawiamy na true
    //Jeżeli nie to zmienna przyjmuje wartość false
    for(int i=0; i< users_size; i++){
        if((*users)[i].descriptor == connection_socket_descriptor){
            descriptor_exist = true;
            pthread_mutex_unlock(users_mutex);
            break;
        }
        else{
            descriptor_exist = false;
        }
    }

    //jeżeli nie ma jeszcze użytkoniwka o podanym deskryptorze tworzymy go
    if(descriptor_exist == false){
        struct User user;
        user.nick = "";
        user.descriptor = connection_socket_descriptor;
        user.room = 0;
        users->push_back(user); //dodajemy nowego użytkownika do wektora użytkowników
        cout<< "Dodano użytkownika. Jego deskryptor to: " << connection_socket_descriptor << endl;
        pthread_mutex_unlock(users_mutex);

        //Wypełnienie struktury, przekazanie do wątku danych
        t_data->user.descriptor = connection_socket_descriptor;
        t_data->users = users;
        t_data->users_mutex = users_mutex;
        t_data->check = false;

        for(int i=0;i<6;i++){
            t_data->rooms_mutex[i] = rooms_mutex[i];
        }

        //Stworzenie nowego watku
        create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void*) t_data); 
        if(create_result){
            cout<< "ERROR! Próba utworzenia wątku dla klienta nie powiodła się. Kod błędu: "<< create_result <<endl;
            exit(-1);
        }
    }


}

int main(int argc, char* argv[]) {

    int server_socket_descriptor, connection_socket_descriptor, bind_result, listen_result;
    char reuse_addr_val = 1;
    
    //nowy obiekt struktury używanej przez funkcję interfejsu gniazd
    struct sockaddr_in server_addr;
    
    //Wektor danych o każdym kliencie
    vector<User> users; 

    
    //Obsługa błędu uruchamiania serwera
    if (argc < 3){
        cout << "ERROR! Drugi argument to port serwera, a trzeci to adres serwera\n";
        return 0;
    }

    
    //wypełnienie struktury gniazd serwera
    memset(&server_addr,0,sizeof(struct sockaddr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(atoi(argv[1]));
    server_addr.sin_addr.s_addr = inet_addr(argv[2]);

    //tworzenie nowego gniazda, do komunikacji sieciowej, zmienna przechowuje deskryptor gniazda 
    server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0); 

    //Obsługa błędów funkcji socket()
    if(server_socket_descriptor < 0){
        cout<< "ERROR! Nie udało się utworzyć gniazda do komunikacji sieciowej serwera\n";
        return 0;
    }

    //ustawiamy opcje gniazda
    setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*) &reuse_addr_val, sizeof(reuse_addr_val));

    //związanie gniazda z adresem lokalnej maszyny
    bind_result = bind(server_socket_descriptor, (struct sockaddr*) &server_addr, sizeof(struct sockaddr));

    //obsługa błędów funkcji bind()
    if(bind_result < 0){
        cout<<"ERROR! Połączenie danego aresu z gniazdem nie powiodło się\n";
        return 0;
    }

    //przygotowanie gniazda na odbieranie zgłoszeń i określenie rozmaru kolejki żądań
    listen_result = listen(server_socket_descriptor, SIZE_OF_QUEUE);

    //Obsługa błędów funkcji listen()
    if(listen_result < 0){
        cout<<"ERROR! Błąd przy próbie ustalenie rozmiaru kolejki\n";
        return 0;
    }


    

    while(1){
        
        //pobranie zgłoszenia z kolejki gniazd strumieniowych lub oczekiwanie na nie (połączenie klient-serwer)
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);

        //Obsługa błędów funkcji accept()
        if(connection_socket_descriptor < 0){
            cout << "ERROR! Coś poszło nie tak przy próbie utworzenia gniazda dla połączenia klient-serwer\n";
            return 0;
        }

        handleConnection(connection_socket_descriptor, &users, &users_mutex, rooms_mutex);

    }

    //zamknięcie gniazda i usunięcie jego deskryptora
    close(server_socket_descriptor);

    return 0;
}
