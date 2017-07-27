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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBManager implements ToDoListManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    
    private static SessionFactory factory;

    public DBManager(String toDoListName) {
        //create a DB Manager
        //set up DB w toDoListName
        //write Title of DB as first line
        //if not created config-- create
        //if exists-- validate
        //TODO
        
        Configuration cfg = new Configuration();
        cfg.configure();
        factory = cfg.buildSessionFactory();
    }

    /**
     * Add an item to the database
     * 
     * @return the item to add
     */
    public String addItem(String item) {
        //TODO idea-- method to  open/begin session
        
        Session session = factory.openSession();

        session.beginTransaction();
        Item todoItem = new Item();
        
        int listNumber = countItems() + 1;
        
        todoItem.setListNumber(listNumber);
        todoItem.setTask(item);
        todoItem.setDateCreated(getDate());
        todoItem.setTimeCreated(getTime());

        session.save(todoItem);
        session.getTransaction().commit();
        
        return "Item added: " + item;
    }
    
    private static int countItems() {
        Session session = factory.openSession();
        session.beginTransaction();
        
        String hql = "SELECT COUNT(item_id) FROM " + Item.class.getName();
        Query query = session.createQuery(hql);
        
        String response = query.list().toString();
        response = response.substring(1, response.indexOf("]"));
        int itemCount = Integer.parseInt(response);
        
        session.getTransaction().commit();
        return itemCount;
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
    
    /**
     * Remove an item from the database based on its item number
     * 
     * @return item.toString()   the string value of the item retrieved from the database
     */
    public String removeItem(int itemNumber) {
        Session session = factory.openSession();

        session.beginTransaction();
        
        Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", itemNumber)).uniqueResult();
        if (item != null){
            session.delete(item);
            int listNumberDeleted = item.getListNumber();
            updateListNumbers(listNumberDeleted);
        } else {
            return "Error:"; //TODO
        }
        
        session.getTransaction().commit();
        return "Item removed: " + item.toString();
    }
    
    /**
     * Update database values in list_number
     *
     * @param listNumberDeleted   the list_number deleted from the database
     */
    public static void updateListNumbers(int listNumberDeleted) {
        Session session = factory.openSession();
        session.beginTransaction();
        
        String hql = "UPDATE " + Item.class.getName() + " SET list_number = list_number -1 WHERE list_number > " + listNumberDeleted;
        Query query = session.createQuery(hql);
        query.executeUpdate();
        
        session.getTransaction().commit();
    }

    /**
     * Remove an item from the database
     *
     * @return item removed
     */
    public String removeItem(String itemKeyword) {
        ArrayList<Item> items = getAllItems();
        
        for (int n = 0; n < items.size(); n++) {
            if (items.get(n).toString().contains(itemKeyword)) {
                Session session = factory.openSession();
                session.beginTransaction();
                
                session.delete(items.get(n));
                int listNumberDeleted = items.get(n).getListNumber();
                updateListNumbers(listNumberDeleted);
                session.getTransaction().commit();
                return  "Item removed: " + items.get(n).toString();
            }
        }
        return "Error:"; //TODO
    }
    
    /**
     * Get all items from database
     * 
     * @return items   ArrayList of items from database
     */
    private static ArrayList<Item> getAllItems() {
        Session session = factory.openSession();
        session.beginTransaction();
        
        ArrayList<Item> items = new ArrayList<Item>();
        
        for (int n = 1; n <= countItems(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null){
                items.add(item);
            }
        }
        
        session.getTransaction().commit();
        return items;
    }
    
    /**
     * Get all text from database table
     * 
     * @return items   ArrayList of text from database
     */
    public ArrayList<String> getAllText() {
        Session session = factory.openSession();
        session.beginTransaction();
        
        ArrayList<String> text = new ArrayList<String>();
        
        for (int n = 1; n <= countItems(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null){
                text.add(item.toString());
            }
        }
        
        if (text.isEmpty()) {
            text.add("Error: No items in database"); //TODO
        }
        
        session.getTransaction().commit();
        return text;
    }

    /**
     * Get all the items with the keyword.
     * This method is case sensitive
     *
     * @param itemKeyword   the keyword to find the items
     * @return itemsWithKeyword   a list of items with the keyword
     */
    public ArrayList<String> getAllItemsWithKeyword(String itemKeyword) {
        ArrayList<String> allText = getAllText();
        ArrayList<String> itemsWithKeyword = new ArrayList<String>();
        
        for (int n = 1; n < allText.size(); n++) {
            if (allText.get(n).contains(itemKeyword)) {
                itemsWithKeyword.add(allText.get(n));
            }
        }

        if (itemsWithKeyword.isEmpty()) {
            itemsWithKeyword.add("Error: No items matched your keyword");
        }
        return itemsWithKeyword;
    }

    /**
     * Get the item based on the item number
     *
     * @param itemNumber   the item number
     * @return getItem(String itemKeyword)
     */
    public String getItem(int itemNumber) {
        return getItem(itemNumber +"),");
    }

    /**
     * Get the item based on a keyword.
     * If there are two or more items with the keyword, the first in the list will be returned.
     * This method is case sensitive
     *
     * @param itemKeyword   a keyword to find the items
     * @return the item containing the keyword
     */
    public String getItem(String itemKeyword) {
        ArrayList<String> text = getAllText();

        for (int n = 0; n < text.size(); n++) {
            if (text.get(n).contains(itemKeyword)) {
                return text.get(n);
            }
        }
        return "Error: Item could not be found";    }
}
