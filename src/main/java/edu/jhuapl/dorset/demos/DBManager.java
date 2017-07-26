/*
 * Copyright 2017 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
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
 */
package edu.jhuapl.dorset.demos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBManager implements ToDoListManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    //TODO fill in class

    public DBManager(String toDoListName) {
        //create a DB Manager
        //set up DB w toDoListName
        //write Title of DB as first line
    }

    /*private void writeTitle(File file, String titleExtension) {
        
    }*/

    public String addItem(String item) {
        //add item to end of DB
        //get number + "," + getDate() + "," + getTime() + "," + item
        return "Item added: " + item;
    }

    /**
     * Get the current date
     * Formatted as: MM/dd/yyyy
     *
     * @return the current date
     */
    private String getDate() {
        Date now = new Date();
        DateFormat day = new SimpleDateFormat("MM/dd/yyyy");
        return day.format(now);
    }

    /**
     * Get the current time
     * Formatted as: hh:mm AM/PM
     *
     * @return the current time
     */
    private String getTime() {
        Date now = new Date();
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        return time.format(now);
    }
    
    public String removeItem(int itemNumber) {
        return removeItem(itemNumber + "),");
    }

    public String removeItem(String itemKeyword) {
        //remove item (or rewriter file without item)
        //remember to fix numbers
        return null;
    }

    /*private String rewriteFileWithoutRemoved(ArrayList<String> text, String itemKeyword) {
        return null;
    }*/

    /*private void rewriteItem(String text, BufferedWriter bufferedWriter, int counter) throws IOException {
        
    }*/

    /*private int getIndexToStartItem(String item) {
        return 0;
    }*/

    public ArrayList<String> getAllText() {
        ArrayList<String> text = new ArrayList<String>();
        //get all text from DB and put into text
        //text.add();
        return text;
    }

    public ArrayList<String> getAllItemsWithKeyword(String itemKeyword) {
        
        return null;
    }

    /*private void checkEmpty(ArrayList<String> itemsWithKeyword) {

    }*/

    public String getItem(int itemNumber) {
        return null;
    }

    public String getItem(String itemKeyword) {
        return null;
    }
}
