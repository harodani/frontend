/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.netinfservice.netinf.node.search;

import project.cs.netinfutilities.metadata.Metadata;

/**
 * A search result implementation that encodes one search result entity.
 * 
 * @author Kim-Anh Tran
 *
 */
public class SearchResultImpl implements SearchResult {
	
	/** The hash of the found entity. */
	private String mHash;
	
	/** The hash algorithm used for creating the hash. */
	private String mHashAlg;
	
	/** The metadata associated with that hash. */
	private Metadata mMetadata;
	
	/**
	 * Creates a search object entity.
	 * 
	 * @param hash			The hash corresponding to this search result
	 * @param hashAlgorithm	The algorithm used for creating the hash
	 * @param metadata		The meta data corresponding to this search result
	 */
	public SearchResultImpl(String hash, String hashAlgorithm, Metadata metadata) {
		mHash = hash;
		mHashAlg = hashAlgorithm;
		mMetadata = metadata;
	}

	/**
	 * Returns a Metadata class object that contains the metadata.
	 * 
	 * @return
	 *     Metadata object with metadata.
	 */
	@Override
	public Metadata getMetaData() {
		return mMetadata;
	}

	/**
	 * Returns the Hash from the search result.
	 * 
	 * @return
	 *     The hash.
	 */
	@Override
	public String getHash() {
		return mHash;
	}

	/**
	 * Returns the Hash Algorithm used to hash the object from the search result.
	 * 
	 * @return
	 *     The hash algorithm used to hash the object.
	 */
	@Override
	public String getHashAlgorithm() {
		return mHashAlg;
	}

}
