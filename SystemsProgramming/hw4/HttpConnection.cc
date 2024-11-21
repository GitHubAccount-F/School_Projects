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

#include <stdint.h>
#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <map>
#include <string>
#include <vector>

#include "./HttpRequest.h"
#include "./HttpUtils.h"
#include "./HttpConnection.h"

using std::map;
using std::string;
using std::vector;

namespace hw4 {

static const char* kHeaderEnd = "\r\n\r\n";
static const int kHeaderEndLen = 4;

bool HttpConnection::GetNextRequest(HttpRequest* const request) {
  // Use WrappedRead from HttpUtils.cc to read bytes from the files into
  // private buffer_ variable. Keep reading until:
  // 1. The connection drops
  // 2. You see a "\r\n\r\n" indicating the end of the request header.
  //
  // Hint: Try and read in a large amount of bytes each time you call
  // WrappedRead.
  //
  // After reading complete request header, use ParseRequest() to parse into
  // an HttpRequest and save to the output parameter request.
  //
  // Important note: Clients may send back-to-back requests on the same socket.
  // This means WrappedRead may also end up reading more than one request.
  // Make sure to save anything you read after "\r\n\r\n" in buffer_ for the
  // next time the caller invokes GetNextRequest()!

  // STEP 1:
  // Buffer contains a request to process/return

  size_t position;
  if ((position = buffer_.find(kHeaderEnd)) != std::string::npos) {
    *request = ParseRequest(buffer_.substr(0, position));
    buffer_.erase(0, position + kHeaderEndLen);
  } else {  // Buffer doesn't contain a request yet
    string add;
    char buf[1024];
    int size = 1;

    while (true) {
      size = WrappedRead(fd_, reinterpret_cast<unsigned char *>(buf), 1024);
      // invalid read
      if (size == -1) {
        return false;
      }
      string output(buf, size);
      // connection drop
      if (size == 0) {
        break;
      }
      add += output;
      // read fewer than expected, break
      if (size < 1024) {
        break;
      }
    }
    buffer_ += add;
    // retrieve request
    position = buffer_.find(kHeaderEnd);
    if (position != std::string::npos) {
      *request = ParseRequest(buffer_.substr(0, position));
      buffer_.erase(0, position + kHeaderEndLen);
    } else {
      if (buffer_.size() > 0) {
        // attempts to read what's currently in buffer
        *request = ParseRequest(buffer_);
        buffer_.clear();
      } else {
        return false;
      }
    }
  }
  return true;
}

bool HttpConnection::WriteResponse(const HttpResponse& response) const {
  string str = response.GenerateResponseString();
  int res = WrappedWrite(fd_,
                         reinterpret_cast<const unsigned char*>(str.c_str()),
                         str.length());
  if (res != static_cast<int>(str.length()))
    return false;
  return true;
}


HttpRequest HttpConnection::ParseRequest(const string& request) const {
  HttpRequest req("/");  // by default, get "/".

  // Plan for STEP 2:
  // 1. Split the request into different lines (split on "\r\n").
  // 2. Extract the URI from the first line and store it in req.URI.
  // 3. For the rest of the lines in the request, track the header name and
  //    value and store them in req.headers_ (e.g. HttpRequest::AddHeader).
  //
  // Hint: Take a look at HttpRequest.h for details about the HTTP header
  // format that you need to parse.
  //
  // You'll probably want to look up boost functions for:
  // - Splitting a string into lines on a "\r\n" delimiter
  // - Trimming whitespace from the end of a string
  // - Converting a string to lowercase.
  //
  // Note: If a header is malformed, skip that line.


  // STEP 2:
  vector<string> subs;

  // Split the string
  boost::split(subs, request, boost::is_any_of("\r\n"));
  vector<string> breakApart;

  string lineOne = subs[0];
  boost::trim(lineOne);

  boost::split(breakApart, lineOne, boost::is_any_of(" "));
  boost::trim(breakApart[1]);
  req.set_uri(breakApart[1]);

  // work on headers
  string replace(":");
  string replaceWith("");
  for (int i = 1; i < static_cast<int>(subs.size()); i++) {
    string header = subs[i];
    boost::trim(header);
    boost::split(breakApart, header, boost::is_any_of(" "));
    if (breakApart.size() != 2) {
      // malformed header
      continue;
    }
    // remove : in headers
    boost::replace_all(breakApart[0], replace, replaceWith);

    // lowercase
    boost::algorithm::to_lower(breakApart[1]);
    boost::algorithm::to_lower(breakApart[0]);

    req.AddHeader(breakApart[0], breakApart[1]);
  }
  return req;
}


}  // namespace hw4
