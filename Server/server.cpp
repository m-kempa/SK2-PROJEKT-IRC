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
    bool errorHandling;
    pthread_mutex_t* users_mutex;
    pthread_mutex_t rooms_mutex[6];
}; 




//Funckja opróżnia bufor z śmieci pozostawionych po operacjach 
//Po operacji w buforze zostają dwa zbędne bity
//Więc odczytujemy je aby wyczyścić bufor
void ThrowingRubbish(thread_data_t *th_data) {
    char buf[2];
    read(th_data -> user.descriptor, buf, sizeof(buf));
}





//Funkcja uzupełnia nick zerem z lewej strony jeśli jego wielkość nie jest dwucyfrowa
string NickZeroAdd(thread_data_t *th_data) {
    string len;
    if(th_data -> user.nick.size() < 10) {len = "0" + to_string(th_data -> user.nick.length());}
    else {len = to_string(th_data -> user.nick.length());}
    return len;
}


//Funkcja wysyła nowo nadaną nazwe pokoju wraz z id pokoju
void NewRoomAdded(thread_data_t *th_data) {

    int room;
    char* lengthReaded = new char[2];
    memset(lengthReaded, 0, 2);
    char length[2];
    string readName = "";
    int pom,read_result;
    int numberReaded = 0;

    room = th_data -> user.room;

    //Odczytywanie długości nowej nazwy pokoju
    //Wiadomość może być zapisana na maksymalnie bajtach
    while(numberReaded < 2) {

        read_result = read(th_data -> user.descriptor, lengthReaded, 2 - numberReaded);
        if(read_result == -1) {
            th_data->errorHandling = true;
            cout << "ERROR! Error trying to read the message length." << endl;
            return;
        }
        else if(read_result == 0) {
            th_data->errorHandling = true;
            cout << "ERROR! User disconnected while trying to read the message length." << endl;
            return;
        }
        readName = readName + lengthReaded;
        numberReaded = numberReaded + read_result;

        if(numberReaded == 2) break;

        //Czyszczenie
        delete lengthReaded;
        lengthReaded = new char[2-numberReaded];
        memset(lengthReaded, 0, 2-numberReaded);
    }

    strcpy(length, readName.c_str());

    pom = atoi(length);
    char* messBuff = new char[pom];
    memset(messBuff, 0, pom);
    
    //Czyszczenie zmiennych
    numberReaded = 0;
    readName = "";

    //Odczytywanie nowej nazwy pokoju o wcześniej odczytanej długości
    while(numberReaded <= pom) {
        read_result = read(th_data -> user.descriptor, messBuff, pom-numberReaded);
        if(read_result == -1) {
            th_data->errorHandling = true;
            cout << "ERROR! Error trying to read the message." << endl;
            return;
        }
        else if(read_result == 0) {
            th_data->errorHandling = true;
            cout << "ERROR! User disconnected while trying to read the message" << endl;
            return;
        }

        readName = readName + messBuff;
        numberReaded = numberReaded + read_result;

        if(numberReaded == pom) break;

        delete messBuff;
        messBuff = new char[pom-numberReaded];
        memset(messBuff, 0, pom-numberReaded);
    }

    delete messBuff;

    string NameLength(length);
    //Tworzenie łańcucha znaków z odpowiedzią do klientów
    string answer = "roomA" + to_string(room) + NameLength + readName + "\n";
    cout<<"Answer to client: "<<answer<<endl;
    int write_result;
    char* messageToWrite = new char[answer.size()];
    int answerLength = answer.size();
    strcpy(messageToWrite, answer.c_str());
    char temp[answer.size()];
    strcpy(temp, answer.c_str());
    numberReaded = 0;


    pthread_mutex_lock(&th_data->rooms_mutex[th_data -> user.room]);
    int size = th_data->users->size();
    for(int i=0; i< size; i++) {
            
            //Wysyłanie odpowiedzi do wszystkich użytkowników
            while(numberReaded < answerLength) {

                write_result = write((*th_data->users)[i].descriptor, messageToWrite, answerLength-numberReaded);
                if(write_result == -1) {
                    th_data->errorHandling = true;
                    cout << "ERROR! Error trying to send the message" << endl;
                    return;
                }
                else if(write_result == 0) {
                    th_data->errorHandling = true;
                    cout << "ERROR! User disconnected while trying to send the message" << endl;
                    return;
                }

                numberReaded = numberReaded + write_result;
                if(numberReaded == answerLength) break;

                delete messageToWrite;
                messageToWrite = new char[answerLength-numberReaded];
                memcpy(messageToWrite, temp+numberReaded, answerLength-numberReaded);
            }
        
        numberReaded = 0;
        delete messageToWrite;
        messageToWrite = new char[answerLength];
        strcpy(messageToWrite, answer.c_str());    
    }


    cout << "Client with the descriptor: " << th_data -> user.descriptor << " add the room on the slot number: " << th_data -> user.room << endl;
    pthread_mutex_unlock(&th_data->rooms_mutex[th_data -> user.room]);
}



//Funkcja obługująca mechanizm wiadomości (pobiera długość wiadomości oraz treść przesłane przez klienta)
//Po tym przekazuję odczytaną wiadomość do pokoju w którym znajduje się klient
void ReceiveMessegeAndSendToRoom(thread_data_t *th_data) {
    char* lengthReaded = new char[3];
    memset(lengthReaded, 0, 3);
    char length[3];
    string readMessage = "";
    int pom,read_result;
    int numberReaded = 0;

    //Odczytywanie długości wiadomości użytkownika
    //Wiadomość może być zapisana na maksymalnie 4 bajtach
    while(numberReaded < 3) {

        read_result = read(th_data -> user.descriptor, lengthReaded, 3 - numberReaded);
        if(read_result == -1) {
            th_data->errorHandling = true;
            cout << "ERROR! Error trying to read the message length" << endl;
            return;
        }
        else if(read_result == 0) {
            th_data->errorHandling = true;
            cout << "ERROR! User disconnected while trying to read the message length" << endl;
            return;
        }
        readMessage = readMessage + lengthReaded;
        numberReaded = numberReaded + read_result;

        if(numberReaded == 3) break;

        //Czyszczenie
        delete lengthReaded;
        lengthReaded = new char[3-numberReaded];
        memset(lengthReaded, 0, 3-numberReaded);
    }

    strcpy(length, readMessage.c_str());

    pom = atoi(length);
    char* messBuff = new char[pom];
    memset(messBuff, 0, pom);
    
    //Czyszczenie zmiennych
    numberReaded = 0;
    readMessage = "";

    //Odczytywanie wiadomości od użytkownika o odczytanej wyżej długości
    while(numberReaded <= pom) {
        read_result = read(th_data -> user.descriptor, messBuff, pom-numberReaded);
        if(read_result == -1) {
            th_data->errorHandling = true;
            cout << "ERROR! Error trying to read the message." << endl;
            return;
        }
        else if(read_result == 0) {
            th_data->errorHandling = true;
            cout << "ERROR! User disconnected while trying to read the message" << endl;
            return;
        }

        readMessage = readMessage + messBuff;
        numberReaded = numberReaded + read_result;

        if(numberReaded == pom) break;

        delete messBuff;
        messBuff = new char[pom-numberReaded];
        memset(messBuff, 0, pom-numberReaded);
    }

    delete messBuff;

    string MessageLength(length);
    //Tworzenie łańcucha znaków z odpowiedzią do klientów
    string answer = "messU" + NickZeroAdd(th_data) + th_data -> user.nick + MessageLength + readMessage + "\n";
    //cout<<"odpowiedz do klienta: "<<answer<<endl;
    int write_result;
    char* messageToWrite = new char[answer.size()];
    int answerLength = answer.size();
    strcpy(messageToWrite, answer.c_str());
    char temp[answer.size()];
    strcpy(temp, answer.c_str());
    numberReaded = 0;


    pthread_mutex_lock(&th_data->rooms_mutex[th_data -> user.room]);
    int size = th_data->users->size();
    for(int i=0; i< size; i++) {
        if((*th_data->users)[i].room == th_data -> user.room) {
            
            //Wysyłanie odpowiednio przygotowanej wiadomości do pokoju 
            while(numberReaded < answerLength) {

                write_result = write((*th_data->users)[i].descriptor, messageToWrite, answerLength-numberReaded);
                if(write_result == -1) {
                    th_data->errorHandling = true;
                    cout << "ERROR! Error trying to send the message" << endl;
                    return;
                }
                else if(write_result == 0) {
                    th_data->errorHandling = true;
                    cout << "ERROR! User disconnected while trying to send the message" << endl;
                    return;
                }

                numberReaded = numberReaded + write_result;
                if(numberReaded == answerLength) break;

                delete messageToWrite;
                messageToWrite = new char[answerLength-numberReaded];
                memcpy(messageToWrite, temp+numberReaded, answerLength-numberReaded);
            }
        }
        numberReaded = 0;
        delete messageToWrite;
        messageToWrite = new char[answerLength];
        strcpy(messageToWrite, answer.c_str());    
    }


    cout << "Client, descriptor number: " << th_data -> user.descriptor << " successfully sent a message to room number: " << th_data -> user.room << endl;
    pthread_mutex_unlock(&th_data->rooms_mutex[th_data -> user.room]);
}


//Funkcja zmienia wcześniejszy pokój klienta na nowy do którego się przeniósł
//Zwracany jest stary numer pokoju w celu wysłania informacji do opuszczanego pokoju
int ClientChangeRoomNumber(thread_data_t *th_data) {
    int old_room;
    char buf_room[1];
    int size;
    int read_result;

    //Pobieranie numeru pokoju
    while((read_result = read(th_data -> user.descriptor, buf_room, sizeof(buf_room))) == 0) {
        if(read_result == -1) {
            cout << "ERROR! An error occurred while reading the room number" << endl;
            return -1;
        }
    }
    
    old_room = th_data -> user.room;
    th_data -> user.room = atoi(buf_room);
    pthread_mutex_lock(th_data->users_mutex);
    size = th_data->users->size();

    //Aktualizacja numeru pokoju na nowy w wektorze użytkowników
    for(int i=0; i<size; i++) {
        if(th_data -> user.descriptor == (*th_data->users)[i].descriptor) {
            (*th_data->users)[i].room = atoi(buf_room); 
            break;
        }         
    }
    pthread_mutex_unlock(th_data->users_mutex);
    cout << "Client, descriptor number: " << th_data -> user.descriptor << " changed room " << th_data -> user.room << endl;
    return old_room;
}


//Funkcja wysyła informacje zwrotną do opuszczanego pokoju lub do pokoju do którego przenosi się klient
void FeedbackToOthersInRoom(thread_data_t *th_data, int room, string mode) {

    string answer;

    //Informacja zwrotna do pokoju który opuszcza klient
    if(mode == "old") answer = "roomL" + NickZeroAdd(th_data) + th_data -> user.nick + "\n"; 
    
    //Informacja zwrotna do pokoju do którego dołącza klient
    else if(mode == "new") answer = "roomJ" + NickZeroAdd(th_data) + th_data -> user.nick + "\n"; 
    
    //cout<<"Odpowiedź do klienta: " << answer <<endl;
    int write_result;
    int size = th_data->users->size();
    int writeInteration = 0;
    char temp[answer.size()];
    strcpy(temp, answer.c_str());
    char* RoomBuf = new char[answer.size()];
    strcpy(RoomBuf, answer.c_str());
    int length = answer.size();

    pthread_mutex_lock(&th_data->rooms_mutex[room]); 
    for(int i=0; i< size; i++) {
        
        //znajdujemy klientów o podanym pokoju w wektorze użytkowników
        if((*th_data->users)[i].room == room) {

            while(writeInteration < length) {

                write_result = write((*th_data->users)[i].descriptor, RoomBuf, length-writeInteration);
                if(write_result == -1) {
                    th_data->errorHandling = true;
                    cout << "Error! The room change message has not been sent." << endl;
                    return;
                }
                else if(write_result == 0) {
                    th_data->errorHandling = true;
                    cout << "Error! Client hung up while sending room change message." << endl;
                    return;
                }


                writeInteration =  writeInteration + write_result;
                if(writeInteration == length) break;
                
                //Czyszczenie 
                delete RoomBuf;
                RoomBuf = new char[length-writeInteration];
                memcpy(RoomBuf, temp+writeInteration, length-writeInteration); //kompiowanie wielkości bajtów
            }
        }   

        delete RoomBuf;
        writeInteration = 0;
        RoomBuf = new char[length];
        strcpy(RoomBuf, answer.c_str()); //kopiujemy odpowiedz do RoomBuf
    }
    pthread_mutex_unlock(&th_data->rooms_mutex[room]);
}





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
            cout << "Reading error - thread ending" << endl;
            break;
        }
        else if (character > 0) {
            cout<<"option: "<<mode<<endl;
            //------------ OPCJA login wszystkie operacje dotyczące logowania klienta-----
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
                else{
                    MaxClients = false;
                }
                pthread_mutex_unlock(th_data->users_mutex);


                //---Sprawdzanie długości nicku klienta (nick może mieć długość od 1 do 99)
                while(number < 2) {
                    read_result = read(th_data -> user.descriptor, actualLength, 2 - number);
                    if(read_result == -1) {
                        cout << "EROR! Could not read nick length" << endl;
                        ReadError = true;
                    }
                    else if(read_result == 0) {
                        cout << "ERROR! The client disconnected while reading the nickname length" << endl;
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
                        cout << "ERROR! Error reading nickname" << endl;
                        ReadError =  true;
                    }
                    else if(read_result == 0) {
                        cout << "ERROR! The client disconnected while reading the nickname"<< endl;
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
                    cout << "There is already someone in the user vector with the given nickname" << endl;
                    SameLoginExist =  true;
                    pthread_mutex_unlock(th_data->users_mutex);
                    }

                if((*th_data->users)[i].descriptor == th_data -> user.descriptor) user_index = i;
                }




                //------Sprawdzamy czy zadane wymagania zostały spełnione i wysyłamy odpowiedni feedback do klienta
                if(SameLoginExist == false && ReadError == false && MaxClients == false) {
                    cout << "Add nick: " << nick << " for client with descriptor: " << th_data -> user.descriptor << endl;
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
                    break;} 
                
                else {
                    pthread_mutex_lock(&th_data->rooms_mutex[0]);
                    //Wysłanie do klienta odpowiedzi 2 = niepowodzenie 
                    write(th_data -> user.descriptor, "2\n", 2 * sizeof(char)); 
                    pthread_mutex_unlock(&th_data->rooms_mutex[0]);
                    break;}

                ThrowingRubbish(th_data); // czyszczenie buffora
            }

            //--------OPCJA cRoom Wysłanie wiadomości do innych klientów odnoście zmiany pokoju przez użytkownika
            else if(strcmp( mode, "cRoom") == 0){
                int old_room = ClientChangeRoomNumber(th_data);
                if(old_room == -1){
                    break; //jeśli przy pobraniu numeru pokoju coś pójdzie nie tak
                }
                ThrowingRubbish(th_data);
                FeedbackToOthersInRoom(th_data, old_room, "old");
                if(th_data->errorHandling == true) {
                    break; //jeżeli wystąpił jakiś błąd podczas write
                }
                FeedbackToOthersInRoom(th_data, th_data -> user.room, "new");
                if(th_data->errorHandling == true) {
                    break; //jeżeli wystąpił jakiś błąd podczas write
                }

            }

            //--------OPCJA aRoom Wysłanie wiadomości do innych klientów odnoście stworzenia nowego pokoju na wolnym slocie
            else if(strcmp( mode, "aRoom") == 0){
        
                NewRoomAdded(th_data);
                if(th_data->errorHandling == true) {
                    break; //jeżeli wystąpił jakiś błąd podczas write
                }
                ThrowingRubbish(th_data);
            }

            //-------Opcja mSend Przesłanie wiadomości dalej do pokoju w którym znajduje się użytkownik, który napisał wiadomość
            else if(strcmp( mode, "mSend") == 0){
                ReceiveMessegeAndSendToRoom(th_data);
                if(th_data->errorHandling == true){
                    break; // jeżeli wystąpił błąd podczas read/write
                }
                ThrowingRubbish(th_data);
            }

            //-------Opcja close Wysłanie wiadomości o opuszczenia pokoju gdy klient rozłączy się z aplikacją
            else if(strcmp( mode, "close") == 0){
                FeedbackToOthersInRoom(th_data, th_data -> user.room, "old");
                break;
            }
        
        }
        memset(mode, 0, sizeof(mode));
    }


    cout << "Delate client with descriptor: " << th_data -> user.descriptor << endl;
    pthread_mutex_lock(th_data->users_mutex);
    pthread_mutex_lock(&th_data->rooms_mutex[th_data -> user.room]);
    //Usunięcie klienta z wektora użytkowników po zakończeniu komunikacji
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
        cout<< "User added. Its descriptor is: " << connection_socket_descriptor << endl;
        pthread_mutex_unlock(users_mutex);

        //Wypełnienie struktury, przekazanie do wątku danych
        t_data->user.descriptor = connection_socket_descriptor;
        t_data->users = users;
        t_data->errorHandling = false;
        t_data->users_mutex = users_mutex;

        for(int i=0;i<6;i++){
            t_data->rooms_mutex[i] = rooms_mutex[i];
        }

        //Stworzenie nowego watku
        create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void*) t_data); 
        if(create_result){
            cout<< "ERROR! The thread creation for the client has failed. Error code: "<< create_result <<endl;
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
        cout << "ERROR! The second argument is the server port and the third argument is the server address\n";
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
        cout<< "ERROR! Failed to create a socket for server network communication\n";
        return 0;
    }

    //ustawiamy opcje gniazda
    setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*) &reuse_addr_val, sizeof(reuse_addr_val));

    //związanie gniazda z adresem lokalnej maszyny
    bind_result = bind(server_socket_descriptor, (struct sockaddr*) &server_addr, sizeof(struct sockaddr));

    //obsługa błędów funkcji bind()
    if(bind_result < 0){
        cout<<"ERROR! Address connection to socket has failed\n";
        return 0;
    }

    //przygotowanie gniazda na odbieranie zgłoszeń i określenie rozmaru kolejki żądań
    listen_result = listen(server_socket_descriptor, SIZE_OF_QUEUE);

    //Obsługa błędów funkcji listen()
    if(listen_result < 0){
        cout<<"ERROR! Error trying to figure out queue size\n";
        return 0;
    }


    cout<<"\t\t\t\tIRC SERVER"<<endl;

    while(1){
        
        //pobranie zgłoszenia z kolejki gniazd strumieniowych lub oczekiwanie na nie (połączenie klient-serwer)
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);

        //Obsługa błędów funkcji accept()
        if(connection_socket_descriptor < 0){
            cout << "ERROR! Something went wrong trying to create a socket for a client-server connection\n";
            return 0;
        }

        handleConnection(connection_socket_descriptor, &users, &users_mutex, rooms_mutex);

    }

    //zamknięcie gniazda i usunięcie jego deskryptora
    close(server_socket_descriptor);

    return 0;
}
