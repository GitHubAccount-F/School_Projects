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

#include "./QueryProcessor.h"

#include <iostream>
#include <algorithm>
#include <list>
#include <string>
#include <vector>

extern "C" {
  #include "./libhw1/CSE333.h"
}

using std::list;
using std::sort;
using std::string;
using std::vector;

namespace hw3 {

QueryProcessor::QueryProcessor(const list<string>& index_list, bool validate) {
  // Stash away a copy of the index list.
  index_list_ = index_list;
  array_len_ = index_list_.size();
  Verify333(array_len_ > 0);

  // Create the arrays of DocTableReader*'s. and IndexTableReader*'s.
  dtr_array_ = new DocTableReader* [array_len_];
  itr_array_ = new IndexTableReader* [array_len_];

  // Populate the arrays with heap-allocated DocTableReader and
  // IndexTableReader object instances.
  list<string>::const_iterator idx_iterator = index_list_.begin();
  for (int i = 0; i < array_len_; i++) {
    FileIndexReader fir(*idx_iterator, validate);
    dtr_array_[i] = fir.NewDocTableReader();
    itr_array_[i] = fir.NewIndexTableReader();
    idx_iterator++;
  }
}

QueryProcessor::~QueryProcessor() {
  // Delete the heap-allocated DocTableReader and IndexTableReader
  // object instances.
  Verify333(dtr_array_ != nullptr);
  Verify333(itr_array_ != nullptr);
  for (int i = 0; i < array_len_; i++) {
    delete dtr_array_[i];
    delete itr_array_[i];
  }

  // Delete the arrays of DocTableReader*'s and IndexTableReader*'s.
  delete[] dtr_array_;
  delete[] itr_array_;
  dtr_array_ = nullptr;
  itr_array_ = nullptr;
}

// This structure is used to store a index-file-specific query result.
typedef struct {
  DocID_t doc_id;  // The document ID within the index file.
  int     rank;    // The rank of the result so far.
} IdxQueryResult;

///// Private Method //////
/*
Input - container: Stores query results for each index file
  query: Query we are using to parse file
  index: IndexTableReader associated with index file
  read: DocTableReader associated with index file
Output - Goes through query, updating container appropriately. Removes elements
inside container if it doesn't satisfy the query
*/
void ModifyDocIdsList(vector<IdxQueryResult>* container,
                      const vector<string>& query,
                      IndexTableReader* index,
                      DocTableReader* read) {
  vector<IdxQueryResult>& refer = *container;
  // If query has more than 1 word
  for (int j = 1; j < static_cast<int>(query.size()); j++) {
    // next word
    DocIDTableReader* hold = index->LookupWord(query[j]);
    if (hold == nullptr) {
      // index file doesn't contain word, exit function
      refer.clear();
      break;
    }
    list<DocIDElementHeader> docIds = hold->GetDocIDList();
    // docId list was empty, meaning index file didn't match query
    if (docIds.size() == 0) {
      // exit function
      delete hold;
      refer.clear();
      break;
    }
    // Go through docIdList and verify each file is in the new docIds list
    for (int k = 0; k < static_cast<int>(refer.size()); k++) {
      bool test = 0;
      for (DocIDElementHeader num : docIds) {
        // checks if container, representing query, is in docId list
        // if found, update rank
        if (num.doc_id == refer[k].doc_id) {
          test = 1;
          refer[k].rank += num.num_positions;
          break;
        }
      }
      // case where docid wasn't found in the new list of docids
      if (test == 0) {
        refer.erase(refer.begin() + k);
        k -= 1;
      }
    }
    delete hold;
  }
}

vector<QueryProcessor::QueryResult>
QueryProcessor::ProcessQuery(const vector<string>& query) const {
  Verify333(query.size() > 0);

  // STEP 1.
  // (the only step in this file)
  // Process each word
  // used to store each query result
  vector<QueryProcessor::QueryResult> final_result;
  // Iterate per index file
  for (int i = 0; i < static_cast<int>(index_list_.size()); i++) {
    // for each index file
    IndexTableReader* index = itr_array_[i];
    DocTableReader* read = dtr_array_[i];
    // containers
    vector<IdxQueryResult> container;


    // Get initial query list, based off first word in query
    DocIDTableReader* hold = index->LookupWord(query[0]);
    if (hold == nullptr) {
      // to next index file if word search failed
      continue;
    }

    list<DocIDElementHeader> docIds = hold->GetDocIDList();
    // check to make sure docIds isn't empty
    if (docIds.size() == 0) {
      // to next index file
      delete hold;
      continue;
    }
    // store initial docIds and rank
    for (DocIDElementHeader element : docIds) {
      IdxQueryResult contain;
      contain.doc_id = element.doc_id;
      contain.rank = element.num_positions;
      container.push_back(contain);
    }

    // if query length is > 1
    if (query.size() > 1) {
      ModifyDocIdsList(&container, query, index, read);
    }

    // we didn't find any files, if so continue
    if (container.size() == 0) {
      delete hold;
      continue;
    }
    // We finished comparing total query to index file, build QueryResults
    for (int i = 0; i < static_cast<int>(container.size()); i++) {
      QueryResult temp;
      // get rank
      temp.rank = container[i].rank;
      // get document name
      read->LookupDocID(container[i].doc_id, &temp.document_name);
      final_result.push_back(temp);
    }
    delete hold;
  }

  // Sort the final results.
  sort(final_result.begin(), final_result.end());

  return final_result;
}






}  // namespace hw3
