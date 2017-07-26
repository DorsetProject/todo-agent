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

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import edu.jhuapl.dorset.demos.FileManager;

public class FileManagerTest {

    @Test
    public void testAdd() {
        try {
            FileManager manager = new FileManager("Nicole");
            manager.addItem("this is an item");
            String response = manager.getItem("item");
            manager.removeItem("is an item");
    
            assertTrue(response.contains("is an item"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testRemoveByNumGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            String item = manager.getItem(1);
            item = item.substring(item.indexOf("M,") + 2);
            manager.removeItem(1);
            String response = manager.getItem(item);
            manager.addItem(item);
    
            assertTrue(response.contains("Error:"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testRemoveByNumBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            String response = manager.removeItem(20);
    
            assertTrue(response.contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testRemoveByKeywordGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            manager.addItem("item to r by keyword");
            manager.getItem("item to r");
            String response = manager.removeItem("item to r");
    
            assertTrue(response.contains("Item removed:"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testRemoveByKeywordBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            String response = manager.removeItem("non-existent item");
    
            assertTrue(response.contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetAllText() {
        try {
            FileManager manager = new FileManager("Nicole");      
            String response = manager.getAllText().get(0);
    
            assertTrue(response.contains("TODO List"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetAllItemsWithKeywordGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            ArrayList<String> response = manager.getAllItemsWithKeyword("Buy");
    
            assertTrue(response.size() > 1);
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetAllItemsWithKeywordBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            ArrayList<String> response = manager.getAllItemsWithKeyword("non-existent items");
    
            assertTrue(response.get(0).contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetAllItemsWithDateGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            manager.addItem("Today's new item");
            String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
            ArrayList<String> response = manager.getAllItemsWithKeyword(date);
            manager.removeItem("Today's new item");
    
            assertTrue(response.get(0).contains(date));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetAllItemsWithDateBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            ArrayList<String> response = manager.getAllItemsWithKeyword("July");
    
            assertTrue(response.get(0).contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetItemByNumGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            String response = manager.getItem(1);
    
            assertTrue(response.contains("1),"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetItemByNumBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            String response = manager.getItem(20);
    
            assertTrue(response.contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetItemByKeywordGood() {
        try {
            FileManager manager = new FileManager("Nicole");
            manager.addItem("this is a new item");
            String response = manager.getItem("new item");
            manager.removeItem("a new item");
    
            assertTrue(response.contains("this is a new item"));
        } catch (IOException e) {
            //TODO
        }
    }

    @Test
    public void testGetItemByKeywordBad() {
        try {
            FileManager manager = new FileManager("Nicole");
            String response = manager.getItem("non-existent item");
    
            assertTrue(response.contains("Error: "));
        } catch (IOException e) {
            //TODO
        }
    }
}
