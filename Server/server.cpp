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
    bool check;
}; 


//Funkcja tworząca nowy wątek dla nowego połączenia klienta
//Szablon tej funkcji został zaczerpnięty z materiałów wykładowych
void handleConnection( int connection_socket_descriptor, vector<User>* users, pthread_mutex_t* users_mutex){
    int create_result = 0;

    pthread_t thread1;

    struct thread_data_t *th_data = new thread_data_t();

    bool descriptor_exist = false; 
    pthread_mutex_lock(users_mutex); //blokujemy mutex klienta
    int users_size = users-> size(); //wielkość wektora klientów
    
    //Jeżeli w wketorze użytkowników dany deskryptor istnieje zmienną descriptor_exist ustawiamy na true
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

    //jeżeli nie ma jeszcze użytkoniwka o podanym 
    if(descriptor_exist == false){
        struct User user;
        user.nick = "";
        user.descriptor = connection_socket_descriptor;
        user.room = 0;
        users->push_back(user); //dodajemy nowego użytkownika do wektora użytkowników
        pthread_mutex_unlock(users_mutex);
        cout<< "Dodano użytkownika. Jego deskryptor to: " << connection_socket_descriptor << endl;

        th_data->user.descriptor = connection_socket_descriptor;
        th_data->users = users;
        th_data->users_mutex = users_mutex;
        th_data->check = false;
    }


}

int main(int argc, char* argv[]) {

    int server_socket_descriptor, connection_socket_descriptor, bind_result, listen_result;
    char reuse_addr_val = 1;
    
    //nowy obiekt struktury używanej przez funkcję interfejsu gniazd
    struct sockaddr_in server_addr;

    //Mutex dla użytkowników, zapobiega wpspółbieznej modyfikacji wektora users
    pthread_mutex_t users_mutex = PTHREAD_MUTEX_INITIALIZER;

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


    //Wektor danych o każdym kliencie
    vector<User> users; 

    while(1){
        
        //pobranie zgłoszenia z kolejki gniazd strumieniowych lub oczekiwanie na nie (połączenie klient-serwer)
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);

        //Obsługa błędów funkcji accept()
        if(connection_socket_descriptor < 0){
            cout << "ERROR! Coś poszło nie tak przy próbie utworzenia gniazda dla połączenia klient-serwer\n";
            return 0;
        }

        handleConnection(connection_socket_descriptor, &users, &users_mutex);

    }

    
    //zamknięcie gniazda i usunięcie jego deskryptora
    close(server_socket_descriptor);

    return 0;
}
