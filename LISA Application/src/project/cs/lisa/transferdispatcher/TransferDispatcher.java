/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**

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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
package project.cs.lisa.transferdispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.android.transferdispatcher.providers.HttpProvider;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;

import project.cs.lisa.bluetooth.provider.BluetoothProvider;
import project.cs.lisa.bluetooth.provider.ByteArrayProvider;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

/**
 * The TransferDispatcher. Responsible for Providing stream to IOs/Files.
 * 
 * @author PG NetInf 3, University of Paderborn.
 * @author Paolo Boshini
 * @author Kim-Anh Tran
 * @pat.name Singleton.
 * @pat.task Forces that only one instance of this class exists.
 */
public enum TransferDispatcher {
    
    /**
     * The Transfer Dispatcher instance.
     */
    INSTANCE;
    
    /**
     * The debug tag.
     */
    private static final String TAG = "TransferDispatcher";
    
    /**
     * The bluetooth mac address of this device.
     */
    private String bluetoothMacAddress;

    
    /**
     * The list of available byte array providers.
     */
    private List<ByteArrayProvider> byteArrayProviders;
    
    /**
     * Initializes the Transfer Dispatcher.
     */
    TransferDispatcher() {
        addByteArrayProviders();
        
        BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();

        if ((bluetoothDefaultAdapter != null) && (bluetoothDefaultAdapter.isEnabled())){
            bluetoothMacAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        }   
    }

    /**
     * Adds available ByteArrayProviders to the TransferDispatcher.
     */   
    private void addByteArrayProviders() {
        byteArrayProviders = new ArrayList<ByteArrayProvider>();
        byteArrayProviders.add(new BluetoothProvider());
    }
    
    /**
     * Provides the stream by a given DO.
     * 
     * @param dataObj
     *           The DO.
     * @return Stream to the underlying BO.
     * @throws IOException
     */
    public byte[] getByteArray(InformationObject myIO) throws IOException {
       
       byte[] resultArray;
       String hash    = myIO.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

       List<Attribute> locators = extractLocators(myIO);
       for (Attribute currentLocator : locators) {
           
           try {           
               resultArray = getByteArray(locSel.next(), hash);         
               if (resultArray != null) return resultArray;
                      
            } catch (NetInfNoStreamProviderFoundException e) {
               Log.d(TAG, "No suitable provider could be found for the locators.");
            }
            
       }
    
       throw new IOException("No suitable locator could be found.");
    }
    
    /**
     * Returns the list of locators from a specified information object.
     * 
     * @param io    The information object that contains the list of locators
     * @return      The list of locators
     */
    private List<Atrribute> extractLocators(InformationObject io) {
        List<Attribute> result = new ArrayList<Attribute>();
        List<Attribute> locators = io.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
        
        return locators;
    }
    
    
    /**
     * Given a locator and a file hash, this method provides the byte array corresponding
     * to the hash.
     * 
     * @param locator
     *           The locator from where the file should be fetched
     * @param hash
     *           The hash of the file that will be fetched
     * @return The byte array corresponding to the hash of the file obtained from the address
     *         specified in the locator.
     */
    public byte[] getByteArray(String locator, String hash) throws NetInfNoStreamProviderFoundException {
           Log.d(TAG, "Connecting to the following locator: " + locator);
        
           ByteArrayProvider provider = getByteArrayProvider(locator);
           return (provider != null) ? provider.getByteArray(locator, hash) : null;
    }   
      
    /**
     * Provides the appropriate ByteArrayProvider.
     * 
     * @param url
     *           The locator from where the file will be fetched
     * @return The specific ByteArrayProvider.
     */
    ByteArrayProvider getByteArrayProvider(String locator)  {
       for (ByteArrayProvider provider : byteArrayProviders) {
          if (provider.canHandle(locator)) {
             Log.d(TAG, "Choosing the following provider: " + provider.describe());
             return provider;
          }
       }
       return null;
    }    
    
}