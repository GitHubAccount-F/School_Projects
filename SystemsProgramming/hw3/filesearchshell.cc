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

#include <cstdlib>    // for EXIT_SUCCESS, EXIT_FAILURE
#include <iostream>   // for std::cout, std::cerr, etc.
#include <sstream>

#include "./QueryProcessor.h"

using std::cerr;
using std::endl;
using std::cin;
using std::cout;


// Error usage message for the client to see
// Arguments:
// - prog_name: Name of the program
static void Usage(char* prog_name);

// Your job is to implement the entire filesearchshell.cc
// functionality. We're essentially giving you a blank screen to work
// with; you need to figure out an appropriate design, to decompose
// the problem into multiple functions or classes if that will help,
// to pick good interfaces to those functions/classes, and to make
// sure that you don't leak any memory.
//
// Here are the requirements for a working solution:
//
// The user must be able to run the program using a command like:
//
//   ./filesearchshell ./foo.idx ./bar/baz.idx /tmp/blah.idx [etc]
//
// i.e., to pass a set of filenames of indices as command line
// arguments. Then, your program needs to implement a loop where
// each loop iteration it:
//
//  (a) prints to the console a prompt telling the user to input the
//      next query.
//
//  (b) reads a white-space separated list of query words from
//      std::cin, converts them to lowercase, and constructs
//      a vector of c++ strings out of them.
//
//  (c) uses QueryProcessor.cc/.h's QueryProcessor class to
//      process the query against the indices and get back a set of
//      query results.  Note that you should instantiate a single
//      QueryProcessor  object for the lifetime of the program, rather
//      than  instantiating a new one for every query.
//
//  (d) print the query results to std::cout in the format shown in
//      the transcript on the hw3 web page.
//
// Also, you're required to quit out of the loop when std::cin
// experiences EOF, which a user passes by pressing "control-D"
// on the console.  As well, users should be able to type in an
// arbitrarily long query -- you shouldn't assume anything about
// a maximum line length.  Finally, when you break out of the
// loop and quit the program, you need to make sure you deallocate
// all dynamically allocated memory.  We will be running valgrind
// on your filesearchshell implementation to verify there are no
// leaks or errors.
//
// You might find the following technique useful, but you aren't
// required to use it if you have a different way of getting the
// job done.  To split a std::string into a vector of words, you
// can use a std::stringstream to get the job done and the ">>"
// operator. See, for example, "gnomed"'s post on stackoverflow for
// their example on how to do this:
//
//   http://stackoverflow.com/questions/236129/c-how-to-split-a-string
//
// (Search for "gnomed" on that page. They use an istringstream, but
// a stringstream gets the job done too.)
//
// Good luck, and write beautiful code!

// Input: A stream, a vector<string> to store results
// Output: Reads user input from console, and stores lowercase
// words inside query.
// If bool is false, then error was detected, such as EOF
bool ReadValue(vector<string>* query);


// Input: A string
// Output: Returns a lowercase version of the strings
void toLower(string* word);

int main(int argc, char** argv) {
  if (argc < 2) {
    Usage(argv[0]);
  }
  vector<string> query;
  vector<hw3::QueryProcessor::QueryResult> store;
  list<string> index_list;
  // store all index files
  for (int i = 1; i < argc; i++) {
    index_list.push_back(argv[i]);
  }
  hw3::QueryProcessor processor(index_list);

  // STEP 1:
  // Implement filesearchshell!
  // Probably want to write some helper methods ...
  while (1) {
    bool check = ReadValue(&query);
    if (!check) {
      break;
    }
    store = processor.ProcessQuery(query);
    if (store.size() == 0) {
      // no results
      cout << "  [no results]" << endl;
    }
    for (int i = 0; i < static_cast<int>(store.size()); i++) {
      cout << "  " << store[i].document_name << " "
        << "(" << store[i].rank << ")" << endl;
    }
    // Empty out query
    query.clear();
  }

  return EXIT_SUCCESS;
}

static void Usage(char* prog_name) {
  cerr << "Usage: " << prog_name << " [index files+]" << endl;
  exit(EXIT_FAILURE);
}


bool ReadValue(vector<string>* query) {
  vector<string>& refer = *query;
  cout << "Enter query: " << endl;
  string line;

  // Try do to the conversion; check on cin.good() afterwards.
  std::getline(cin, line);
  if (!cin.good()) {
    return false;
  }
  string word;

  std::istringstream iss(line);

  // read from input
  while (iss >> word) {
    toLower(&word);
    refer.push_back(word);
  }
  return true;
}

void toLower(string* word) {
  string& refer = *word;
  for (char &c : refer) {
    c = std::tolower(c);
  }
}
