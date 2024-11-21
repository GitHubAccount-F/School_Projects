/*
 * Copyright ©2024 Hannah C. Tang.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 333 for use solely during Spring Quarter 2024 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

#include <stdio.h>       // for snprintf()
#include <unistd.h>      // for close(), fcntl()
#include <sys/types.h>   // for socket(), getaddrinfo(), etc.
#include <sys/socket.h>  // for socket(), getaddrinfo(), etc.
#include <arpa/inet.h>   // for inet_ntop()
#include <netdb.h>       // for getaddrinfo()
#include <errno.h>       // for errno, used by strerror()
#include <string.h>      // for memset, strerror()
#include <iostream>      // for std::cerr, etc.

#include "./ServerSocket.h"

extern "C" {
  #include "libhw1/CSE333.h"
}

namespace hw4 {

ServerSocket::ServerSocket(uint16_t port) {
  port_ = port;
  listen_sock_fd_ = -1;
}

ServerSocket::~ServerSocket() {
  // Close the listening socket if it's not zero.  The rest of this
  // class will make sure to zero out the socket if it is closed
  // elsewhere.
  if (listen_sock_fd_ != -1)
    close(listen_sock_fd_);
  listen_sock_fd_ = -1;
}

bool ServerSocket::BindAndListen(int ai_family, int* const listen_fd) {
  // Use "getaddrinfo," "socket," "bind," and "listen" to
  // create a listening socket on port port_.  Return the
  // listening socket through the output parameter "listen_fd"
  // and set the ServerSocket data member "listen_sock_fd_"

  // STEP 1:
  struct addrinfo hints;
  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = ai_family;       // IPv6 (also handles IPv4 clients)
  hints.ai_socktype = SOCK_STREAM;  // stream
  hints.ai_flags = AI_PASSIVE;      // use wildcard "INADDR_ANY"
  hints.ai_flags |= AI_V4MAPPED;    // use v4-mapped v6 if no v6 found
  hints.ai_protocol = IPPROTO_TCP;  // tcp protocol
  hints.ai_canonname = nullptr;
  hints.ai_addr = nullptr;
  hints.ai_next = nullptr;

  // Use argv[1] as the string representation of our portnumber to
  // pass in to getaddrinfo().  getaddrinfo() returns a list of
  // address structures via the output parameter "result".
  struct addrinfo *result;
  int res = getaddrinfo(nullptr, std::to_string(port_).c_str(),
                        &hints, &result);

  // Did addrinfo() fail?
  if (res != 0) {
    return false;
  }

  // Loop through the returned address structures until we are able
  // to create a socket and bind to one.  The address structures are
  // linked in a list through the "ai_next" field of result.
  int store = -1;
  for (struct addrinfo *rp = result; rp != nullptr; rp = rp->ai_next) {
    store = socket(rp->ai_family,
                       rp->ai_socktype,
                       rp->ai_protocol);
    if (store == -1) {
      // Creating this socket failed.  So, loop to the next returned
      // result and try again.
      continue;
    }

    // Configure the socket; we're setting a socket "option."  In
    // particular, we set "SO_REUSEADDR", which tells the TCP stack
    // so make the port we bind to available again as soon as we
    // exit, rather than waiting for a few tens of seconds to recycle it.
    int optval = 1;
    setsockopt(store, SOL_SOCKET, SO_REUSEADDR,
               &optval, sizeof(optval));

    // Try binding the socket to the address and port number returned
    // by getaddrinfo().
    if (bind(store, rp->ai_addr, rp->ai_addrlen) == 0) {
      // Bind worked!  Return to the caller the address family.
      sock_family_ = rp->ai_family;
      break;
    }

    // The bind failed.  Close the socket, then loop back around and
    // try the next address/port returned by getaddrinfo().
    close(store);
    store = -1;
  }

  // Free the structure returned by getaddrinfo().
  freeaddrinfo(result);

  // If we failed to bind, return failure.
  if (store <= 0) {
    return false;
  }
  // Success. Tell the OS that we want this to be a listening socket.
  if (listen(store, SOMAXCONN) != 0) {
    close(store);
    return false;
  }

  // Return to the client the listening file descriptor.
  *listen_fd = store;
  listen_sock_fd_ = store;
  return true;
}

bool ServerSocket::Accept(int* const accepted_fd,
                          std::string* const client_addr,
                          uint16_t* const client_port,
                          std::string* const client_dns_name,
                          std::string* const server_addr,
                          std::string* const server_dns_name) const {
  // Accept a new connection on the listening socket listen_sock_fd_.
  // (Block until a new connection arrives.)  Return the newly accepted
  // socket, as well as information about both ends of the new connection,
  // through the various output parameters.

  // STEP 2:
  struct sockaddr_storage caddr;
  int check;
  socklen_t caddr_len = sizeof(caddr);
  int client_fd = accept(listen_sock_fd_,
                           reinterpret_cast<struct sockaddr *>(&caddr),
                           &caddr_len);
  if (client_fd < 0) {
    return false;
  }
  *accepted_fd = client_fd;

  struct sockaddr_in* sa;  // IPv4
  struct sockaddr_in6* sa6;  // IPv6
  // Get client's DNS name
  char client_host[1024];
  // Get server's IP address and DNS name



  if (caddr.ss_family == AF_INET) {
    sa = reinterpret_cast<struct sockaddr_in*>(&caddr);
    *client_port = (sa->sin_port);

    char address[INET_ADDRSTRLEN];  // store address for client
    inet_ntop(AF_INET, &(sa->sin_addr), address, INET_ADDRSTRLEN);
    *client_addr = address;

    char address2[INET_ADDRSTRLEN];  // store address for server
    socklen_t srvrlen = sizeof(*sa);
    check = getsockname(client_fd, (struct sockaddr *)sa, &srvrlen);
    if (check == -1) {
      return false;
    }
    inet_ntop(AF_INET, &sa->sin_addr, address2, INET_ADDRSTRLEN);
    char server_host[1024];
    getnameinfo((const struct sockaddr *) sa,
                srvrlen, server_host, 1024, nullptr, 0, 0);
    *server_addr = address2;
    *server_dns_name = server_host;
  } else if (caddr.ss_family == AF_INET6) {
    sa6 = reinterpret_cast<struct sockaddr_in6*>(&caddr);
    *client_port = (sa6->sin6_port);

    char address[INET6_ADDRSTRLEN];  // store address for client
    inet_ntop(AF_INET6, &(sa6->sin6_addr), address, INET6_ADDRSTRLEN);
    *client_addr = address;

    char address2[INET_ADDRSTRLEN];  // store address for server
    socklen_t srvrlen = sizeof(*sa6);
    check = getsockname(client_fd, (struct sockaddr *)sa6, &srvrlen);
    if (check == -1) {
      return false;
    }
    inet_ntop(AF_INET6, &sa6->sin6_addr, address2, INET6_ADDRSTRLEN);
    char server_host[1024];
    getnameinfo((const struct sockaddr *) sa6,
                srvrlen, server_host, 1024, nullptr, 0, 0);
    *server_addr = address2;
    *server_dns_name = server_host;
  } else {
    return false;
  }

  // get DNS name for client
  getnameinfo((struct sockaddr*)&caddr, caddr_len,
              client_host, 1024, nullptr, 0, 0);
  *client_dns_name = client_host;

  return true;
}

}  // namespace hw4
