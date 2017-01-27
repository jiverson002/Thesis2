void receiveData(int client_socket); /* void receiveData(int client_socket) */
void initialPacket(char *packet, int client_socket); /* void initialPacket(char *packet, int client_socket) */
void sendMessage(char *packet, int client_socket); /* void sendMessage(char *packet, int client_socket) */
void destinationError(int client_socket, char *invalid, char *send_to, int send_to_len, char *send_from, int send_from_len); /* void destinationError(int client_socket, char *invalid, char *send_to, int send_to_len, char *send_from, int send_from_len) */
int compareClients(char *send_to, int send_to_len); /* int compareClients(char *send_to, int send_to_len) */
void selectCall(int server_socket); /* void selectCall(int server_socket) */
void broadcastMessage(char *packet, int client_socket); /* void broadcastMessage(char *packet, int client_socket) */
void listAllHandles(char *packet, int client_socket); /* void listAllHandles(char *packet, int client_socket) */
void sendHandle(int sending_index); /* void sendHandle(int sending_index) */
void exitClient(char *packet, int client_socket); /* void exitClient(char *packet, int client_socket) */
int global_e = 1; /* int variable_elements = 1 ; */
char **global_a; /* char * * handle_list ; */
int *global_d; /* int * handle_list_fds ; */
int global_c = 0; /* int num_handles_name = 0 ; */
int global_g = 0; /* int num_handles_fd = 0 ; */
int global_b = 3; /* int max_socket = 3 ; */
int global_f; /* int global_socket ; */
int global_h = 0; /* int list_flag = 0 ; */
int global_i = 0; /* int sending_index = 0 ; */
int global_j; /* int list_client ; */

unsigned int __bswap_32(unsigned int a) /*  */{
return __builtin_bswap32(a); /* return __builtin_bswap32(__bsx) */
}
__uint64_t __bswap_64(__uint64_t a) /*  */{
return __builtin_bswap64(a); /* return __builtin_bswap64(__bsx) */
}
int main(int a, char *b) /* int main ( int argc , char * argv [ ] ) { */{
int c = tcpServerSetup(); /* int server_socket = tcpServerSetup ( ) ; */
global_a = malloc(global_e * sizeof(char *)); /* handle_list = malloc ( variable_elements * sizeof ( char * ) ) ; */
global_d = malloc(global_e * sizeof(char)); /* handle_list_fds = malloc ( variable_elements * sizeof ( char ) ) ; */
global_f = c; /* global_socket = server_socket ; */
selectCall(c); /* selectCall ( server_socket ) ; */
close(c); /* close ( server_socket ) ; */
return 0; /* return 0 */
}
void selectCall(int a) /* void selectCall ( int server_socket ) { */{
int b; /* int client_socket ; */
fd_set c; /* fd_set fdvar ; */
FD_ZERO(&c); /* FD_ZERO ( & fdvar ) ; */
FD_SET(a, &c); /* FD_SET ( server_socket , & fdvar ) ; */
global_d[global_g] = a; /* handle_list_fds [ num_handles_fd ] = server_socket ; */
global_g++; /* num_handles_fd ++ ; */
while (mySelect(global_b + 1, (fd_set *)&c, (fd_set *)0, (fd_set *)0, ((void *)0 ))) /* while (mySelect(max_socket + 1, (fd_set *)&fdvar, (fd_set *)0, (fd_set *)0, ((void *)0 ))) */ {
int d; /* int i ; */
for (d = 0; d<global_g; d++) {
int e = global_d[d]; /* int current_fd = handle_list_fds [ i ] ; */
if (FD_ISSET(e, &c)) /* if (FD_ISSET(current_fd, &fdvar)) */ {
if (e == a) /* if (current_fd == server_socket) */ {
b = tcpAccept(a); /* client_socket = tcpAccept ( server_socket ) ; */
FD_SET(b, &c); /* FD_SET ( client_socket , & fdvar ) ; */
global_d[global_g] = b; /* handle_list_fds [ num_handles_fd ] = client_socket ; */
global_g++; /* num_handles_fd ++ ; */
if (b>global_b) /* if (client_socket>max_socket) */ {
global_b = b; /* max_socket = client_socket ; */
}
}
receiveData(e); /* receiveData ( current_fd ) ; */
}
}
FD_ZERO(&c); /* FD_ZERO ( & fdvar ) ; */
for (d = 0; d<global_g; d++) {
FD_SET(global_d[d], &c); /* FD_SET ( handle_list_fds [ i ] , & fdvar ) ; */
}
}
}
void receiveData(int a) /* void receiveData ( int client_socket ) { */{
uint16_t b; /* uint16_t packet_len ; */
int c; /* int message_len ; */
uint8_t d; /* uint8_t flag ; */
char *e = ((void *)0 ); /* char * packet = ( ( void * ) 0 ) ; */
e = malloc(1024); /* packet = malloc ( 1024 ) ; */
c = myRecv(a, e, 1024, 0); /* message_len = myRecv ( client_socket , packet , 1024 , 0 ) ; */
if (!global_h) /* if (!list_flag) */ {
d = *((uint8_t *)(e + 2 ) ); /* flag = * ( ( uint8_t * ) ( packet + 2 ) ) ; */
switch (d) /* switch (flag) */ {
case 1: 
initialPacket(e, a); /* initialPacket ( packet , client_socket ) ; */
break;
case 5: 
sendMessage(e, a); /* sendMessage ( packet , client_socket ) ; */
break;
case 4: 
broadcastMessage(e, a); /* broadcastMessage ( packet , client_socket ) ; */
break;
case 8: 
exitClient(e, a); /* exitClient ( packet , client_socket ) ; */
break;
case 10: 
listAllHandles(e, a); /* listAllHandles ( packet , client_socket ) ; */
break;
}
} else {
if (global_i == global_c) /* if (sending_index == num_handles_name) */ {
global_i = 0; /* sending_index = 0 ; */
global_h = 0; /* list_flag = 0 ; */
} else {
sendHandle(global_i); /* sendHandle ( sending_index ) ; */
}
}
}
void exitClient(char *a, int b) /* void exitClient ( char * packet , int client_socket ) { */{
int c, d = -1; /* int i , index = - 1 ; */
int e; /* int sent ; */
uint8_t f = 9; /* uint8_t flag = 9 ; */
uint16_t g = 3; /* uint16_t packet_len = 3 ; */
char *h; /* char * ret_packet ; */
h = malloc(3); /* ret_packet = malloc ( 3 ) ; */
memcpy(h, &g, 2); /* memcpy ( ret_packet , & packet_len , 2 ) ; */
memcpy(h + 2, &f, 1); /* memcpy ( ret_packet + 2 , & flag , 1 ) ; */
for (c = 0; c<global_g; c++) {
if (global_d[c] == b) /* if (handle_list_fds[i] == client_socket) */ {
d = c; /* index = i ; */
}
}
for (c = d; c<global_g - 1; c++) {
global_d[c] = global_d[c + 1]; /* handle_list_fds [ i ] = handle_list_fds [ i + 1 ] ; */
}
global_g--; /* num_handles_fd -- ; */
for (c = d - 1; c<global_c - 1; c++) {
global_a[c] = global_a[c + 1]; /* handle_list [ i ] = handle_list [ i + 1 ] ; */
}
global_c--; /* num_handles_name -- ; */
e = mySend(b, h, g, 0); /* sent = mySend ( client_socket , ret_packet , packet_len , 0 ) ; */
}
void listAllHandles(char *a, int b) /* void listAllHandles ( char * packet , int client_socket ) { */{
int c; /* int sent ; */
char *d; /* char * ret_packet ; */
uint16_t e; /* uint16_t packet_len ; */
uint8_t f = 11; /* uint8_t flag = 11 ; */
global_h = 1; /* list_flag = 1 ; */
global_j = b; /* list_client = client_socket ; */
e = 3 + 4; /* packet_len = 3 + 4 ; */
d = malloc(e); /* ret_packet = malloc ( packet_len ) ; */
memcpy(d, &e, 2); /* memcpy ( ret_packet , & packet_len , 2 ) ; */
memcpy(d + 2, &f, 1); /* memcpy ( ret_packet + 2 , & flag , 1 ) ; */
memcpy(d + 3, &global_c, 4); /* memcpy ( ret_packet + 3 , & num_handles_name , 4 ) ; */
c = mySend(b, d, e, 0); /* sent = mySend ( client_socket , ret_packet , packet_len , 0 ) ; */
free(d); /* free ( ret_packet ) ; */
}
void sendHandle(int a) /* void sendHandle ( int handle_index ) { */{
uint8_t b, c = 12; /* uint8_t handle_len , flag = 12 ; */
int d; /* int sent ; */
uint16_t e; /* uint16_t packet_len ; */
char *f, *g; /* char * ret_packet , * handle ; */
global_i++; /* sending_index ++ ; */
g = global_a[a]; /* handle = handle_list [ handle_index ] ; */
b = strlen(global_a[a]); /* handle_len = strlen ( handle_list [ handle_index ] ) ; */
e = 3 + 1 + b; /* packet_len = 3 + 1 + handle_len ; */
f = malloc(e); /* ret_packet = malloc ( packet_len ) ; */
memcpy(f, &e, 2); /* memcpy ( ret_packet , & packet_len , 2 ) ; */
memcpy(f + 2, &c, 1); /* memcpy ( ret_packet + 2 , & flag , 1 ) ; */
memcpy(f + 3, &b, 1); /* memcpy ( ret_packet + 3 , & handle_len , 1 ) ; */
memcpy(f + 3 + 1, g, b); /* memcpy ( ret_packet + 3 + 1 , handle , handle_len ) ; */
d = mySend(global_j, f, e, 0); /* sent = mySend ( list_client , ret_packet , packet_len , 0 ) ; */
free(f); /* free ( ret_packet ) ; */
}
void initialPacket(char *a, int b) /* void initialPacket ( char * packet , int client_socket ) { */{
int c, d, e; /* int packet_len , flag , sent ; */
int f; /* int i ; */
char *g = malloc(3), *h; /* char * ret_packet = malloc ( 3 ) , * cmp2 ; */
char *i = a + 4; /* char * handle = packet + 4 ; */
char j = *(a + 3 ); /* char handle_length = * ( packet + 3 ) ; */
d = 2; /* flag = 2 ; */
c = 3; /* packet_len = 3 ; */
i = strtok(i, " "); /* handle = strtok ( handle , " " ) ; */
for (f = 0; f<global_c; f++) {
h = malloc(j); /* cmp2 = malloc ( handle_length ) ; */
memcpy(h, i, j); /* memcpy ( cmp2 , handle , handle_length ) ; */
if (strcmp(global_a[f], h) == 0) /* if (strcmp(handle_list[i], cmp2) == 0) */ {
d = 3; /* flag = 3 ; */
}
free(h); /* free ( cmp2 ) ; */
}
if (d == 2) /* if (flag == 2) */ {
if (global_c == global_e) /* if (num_handles_name == variable_elements) */ {
int k; /* int i ; */
global_e *= 2; /* variable_elements *= 2 ; */
char *l = realloc(global_a, global_e); /* char * tmp = realloc ( handle_list , variable_elements ) ; */
if (l == ((void *)0 )) /* if (tmp == ((void *)0 )) */ {
printf("Error reallocating memory!\n"); /* printf ( "Error reallocating memory!\n" ) ; */
}
int *m = malloc(global_e); /* int * tmp_arr = malloc ( variable_elements ) ; */
for (k = 0; k<global_g; k++) {
m[k] = global_d[k]; /* tmp_arr [ i ] = handle_list_fds [ i ] ; */
}
global_d = m; /* handle_list_fds = tmp_arr ; */
}
global_a[global_c] = malloc(j); /* handle_list [ num_handles_name ] = malloc ( handle_length ) ; */
memcpy(global_a[global_c], i, j); /* memcpy ( handle_list [ num_handles_name ] , handle , handle_length ) ; */
global_c++; /* num_handles_name ++ ; */
}
memcpy(g, &c, 2); /* memcpy ( ret_packet , & packet_len , 2 ) ; */
memcpy(g + 2, &d, 1); /* memcpy ( ret_packet + 2 , & flag , 1 ) ; */
e = mySend(b, g, 3, 0); /* sent = mySend ( client_socket , ret_packet , 3 , 0 ) ; */
}
void sendMessage(char *a, int b) /* void sendMessage ( char * packet , int client_socket ) { */{
uint16_t c; /* uint16_t packet_len ; */
uint8_t d, e, f; /* uint8_t send_to_len , send_from_len , flag ; */
int g = -1, h, i, j; /* int fd_index = - 1 , fd_send , sent , text_len ; */
char *k, *l, *m, *n; /* char * send_to , * send_from , * text , * invalid ; */
f = 5; /* flag = 5 ; */
d = *(a + 3 ); /* send_to_len = * ( packet + 3 ) ; */
l = a + 3 + 1 + d + 1; /* send_from = packet + 3 + 1 + send_to_len + 1 ; */
k = a + 3 + 1; /* send_to = packet + 3 + 1 ; */
e = *(a + 3 + 1 + d ); /* send_from_len = * ( packet + 3 + 1 + send_to_len ) ; */
m = a + 3 + 1 + d + 1 + e; /* text = packet + 3 + 1 + send_to_len + 1 + send_from_len ; */
j = *(uint16_t *)a - 3 - 1 - d - 1 - e; /* text_len = * ( uint16_t * ) packet - 3 - 1 - send_to_len - 1 - send_from_len ; */
if (strlen(m) == 0) /* if (strlen(text) == 0) */ {
j = 1; /* text_len = 1 ; */
m = malloc(1); /* text = malloc ( 1 ) ; */
memcpy(m, " ", 1); /* memcpy ( text , " " , 1 ) ; */
}
n = malloc(d); /* invalid = malloc ( send_to_len ) ; */
memcpy(n, k, d); /* memcpy ( invalid , send_to , send_to_len ) ; */
g = compareClients(k, d); /* fd_index = compareClients ( send_to , send_to_len ) ; */
c = 3 + 1 + e + j; /* packet_len = 3 + 1 + send_from_len + text_len ; */
a = malloc(c); /* packet = malloc ( packet_len ) ; */
memcpy(a, &c, 2); /* memcpy ( packet , & packet_len , 2 ) ; */
memcpy(a + 2, &f, 1); /* memcpy ( packet + 2 , & flag , 1 ) ; */
memcpy(a + 2 + 1, &e, 1); /* memcpy ( packet + 2 + 1 , & send_from_len , 1 ) ; */
memcpy(a + 2 + 1 + 1, l, e); /* memcpy ( packet + 2 + 1 + 1 , send_from , send_from_len ) ; */
memcpy(a + 2 + 1 + 1 + e, m, j); /* memcpy ( packet + 2 + 1 + 1 + send_from_len , text , text_len ) ; */
if (g != -1) /* if (fd_index != -1) */ {
h = global_d[g + 1]; /* fd_send = handle_list_fds [ fd_index + 1 ] ; */
i = mySend(h, a, c, 0); /* sent = mySend ( fd_send , packet , packet_len , 0 ) ; */
} else {
destinationError(b, n, k, d, l, e); /* destinationError ( client_socket , invalid , send_to , send_to_len , send_from , send_from_len ) ; */
}
free(n); /* free ( invalid ) ; */
}
int compareClients(char *a, int b) /* int compareClients ( char * send_to , int send_to_len ) { */{
char *c, *d; /* char * hand , * tmp ; */
int e, f = -1, g, h; /* int i , fd_index = - 1 , cmp , handle_len ; */
for (e = 0; e<global_c; e++) {
h = strlen(global_a[e]); /* handle_len = strlen ( handle_list [ i ] ) ; */
c = malloc(b); /* hand = malloc ( send_to_len ) ; */
d = malloc(b); /* tmp = malloc ( send_to_len ) ; */
memcpy(c, a, b); /* memcpy ( hand , send_to , send_to_len ) ; */
memcpy(d, global_a[e], h); /* memcpy ( tmp , handle_list [ i ] , handle_len ) ; */
g = strcmp(d, c); /* cmp = strcmp ( tmp , hand ) ; */
if (g == 0) /* if (cmp == 0) */ {
return e; /* return i */
}
}
return f; /* return fd_index */
}
void broadcastMessage(char *a, int b) /* void broadcastMessage ( char * packet , int client_socket ) { */{
int c, d, e; /* int i , cur_fd , sent ; */
char *f, *g, *h; /* char * packet_new , * send_from , * text ; */
uint8_t i, j = 5; /* uint8_t send_from_len , flag = 5 ; */
uint16_t k, l, m; /* uint16_t packet_len , text_len , packet_new_len ; */
k = *((uint16_t *)a ); /* packet_len = * ( ( uint16_t * ) packet ) ; */
i = *((uint8_t *)(a + 3 ) ); /* send_from_len = * ( ( uint8_t * ) ( packet + 3 ) ) ; */
l = k - 3 - 1 - i; /* text_len = packet_len - 3 - 1 - send_from_len ; */
h = a + 3 + 1 + i; /* text = packet + 3 + 1 + send_from_len ; */
g = a + 3 + 1; /* send_from = packet + 3 + 1 ; */
if (strlen(h) == 0) /* if (strlen(text) == 0) */ {
l = 1; /* text_len = 1 ; */
h = malloc(1); /* text = malloc ( 1 ) ; */
memcpy(h, " ", 1); /* memcpy ( text , " " , 1 ) ; */
}
m = 3 + 1 + i + l; /* packet_new_len = 3 + 1 + send_from_len + text_len ; */
f = malloc(m); /* packet_new = malloc ( packet_new_len ) ; */
memcpy(f, &m, 2); /* memcpy ( packet_new , & packet_new_len , 2 ) ; */
memcpy(f + 2, &j, 1); /* memcpy ( packet_new + 2 , & flag , 1 ) ; */
memcpy(f + 3, &i, 1); /* memcpy ( packet_new + 3 , & send_from_len , 1 ) ; */
memcpy(f + 3 + 1, g, i); /* memcpy ( packet_new + 3 + 1 , send_from , send_from_len ) ; */
memcpy(f + 3 + 1 + i, h, l); /* memcpy ( packet_new + 3 + 1 + send_from_len , text , text_len ) ; */
for (c = 0; c<global_g; c++) {
d = global_d[c]; /* cur_fd = handle_list_fds [ i ] ; */
if (d != global_f&&d != b) /* if (cur_fd != global_socket&&cur_fd != client_socket) */ {
e = mySend(d, f, k, 0); /* sent = mySend ( cur_fd , packet_new , packet_len , 0 ) ; */
}
}
}
void destinationError(int a, char *b, char *c, int d, char *e, int f) /* void destinationError ( int client_socket , char * invalid , char * send_to , int send_to_len , char * send_from , int send_from_len ) { */{
int g; /* int sent ; */
char *h; /* char * packet ; */
uint16_t i; /* uint16_t packet_len ; */
uint8_t j = 7, k = d; /* uint8_t flag = 7 , len = send_to_len ; */
i = 3 + 1 + d; /* packet_len = 3 + 1 + send_to_len ; */
h = malloc(i); /* packet = malloc ( packet_len ) ; */
memcpy(h, &i, 2); /* memcpy ( packet , & packet_len , 2 ) ; */
memcpy(h + 2, &j, 1); /* memcpy ( packet + 2 , & flag , 1 ) ; */
memcpy(h + 2 + 1, &k, 1); /* memcpy ( packet + 2 + 1 , & len , 1 ) ; */
memcpy(h + 2 + 1 + 1, c, k); /* memcpy ( packet + 2 + 1 + 1 , send_to , len ) ; */
g = mySend(a, h, i, 0); /* sent = mySend ( client_socket , packet , packet_len , 0 ) ; */
}
