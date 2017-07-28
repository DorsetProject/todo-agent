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

    private SessionFactory factory;

    /**
     * Create a DB Manager
     *
     * @param toDoListName   the name of ...TODO
     */
    public DBManager(String toDoListName) {
        //TODO toDoListName not used
        Configuration configuration = new Configuration().configure();
        factory = configuration.buildSessionFactory();        
    }

    /**
     * Add an item to the database
     *
     * @return the item to add
     */
    public String addItem(String item) {        
        Session session = getSession();

        Item todoItem = createItem(item);
        session.save(todoItem);

        endSession(session);
        return "Item added: " + item;
    }
    
    /**
     * Create and get a new Session
     *
     * @return session   the new Session
     */
    private Session getSession() {
        Session session = factory.openSession();
        session.beginTransaction();
        return session;
    }

    /**
     * Commit and close current Session
     *
     * @param session   Session to close
     */
    private void endSession(Session session) {
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Create a new item with given task
     *
     * @param task   the task for the new item
     * @return todoItem   the new item to be added
     */
    private Item createItem(String task) {
        Item todoItem = new Item();

        int listNumber = getItemCount() + 1;
        todoItem.setListNumber(listNumber);
        todoItem.setTask(task);
        todoItem.setDateCreated(getDate());
        todoItem.setTimeCreated(getTime());

        return todoItem;
    }

    /**
     * get the number of items in the database
     *
     * @return itemCount   the number of items in the database
     */
    private int getItemCount() {
        Session session = getSession();

        String hql = "SELECT COUNT(item_id) FROM " + Item.class.getName();
        Query query = session.createQuery(hql);
        String response = getQueryResponse(query);
        int itemCount = Integer.parseInt(response);
        
        endSession(session);
        return itemCount;
    }

    /**
     * Get response from query
     *
     * @param session   the current Session
     * @param hql   the HQL query
     * @return response   the query response
     */
    private String getQueryResponse(Query query) {
        String response = query.list().toString();
        response = response.substring(1, response.indexOf("]"));
        return response;
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
        Session session = getSession();

        Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", itemNumber)).uniqueResult();
        if (item != null){
            deleteItemAndUpdateList(session, item);
        } else {
            logger.error("Item could not be removed");
            return "Error: Item could not be added";
        }

        endSession(session);
        return "Item removed: " + item.toString();
    }

    private void deleteItemAndUpdateList(Session session,Item item) {
        session.delete(item);
        int listNumberDeleted = item.getListNumber();

        updateListNumbers(session,listNumberDeleted);
    }

    /**
     * Update database values in list_number
     *
     * @param listNumberDeleted   the list_number deleted from the database
     */
    public void updateListNumbers(Session session,int listNumberDeleted) {
        String hql = "UPDATE " + Item.class.getName() + " SET list_number = list_number -1 WHERE list_number > "
                        + listNumberDeleted;
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }

    /**
     * Remove an item from the database
     *
     * @return item removed
     */
    public String removeItem(String itemKeyword) {
        ArrayList<Item> items = getAllItems();

        if (items.isEmpty()) {
            logger.error("No item matched request");
            return "Error: Item could not be removed. No item matched your request";
        }

        for (int n = 0; n < items.size(); n++) {
            if (items.get(n).toString().contains(itemKeyword)) {
                Session session = getSession();
                deleteItemAndUpdateList(session, items.get(n));

                endSession(session);
                return  "Item removed: " + items.get(n).toString();
            }
        }
        logger.error("Item could not be removed");
        return "Error: Item could not be removed. No item number or keyword matched your request";
    }

    /**
     * Get all items from database
     * 
     * @return items   ArrayList of items from database
     */
    private ArrayList<Item> getAllItems() {
        Session session = getSession();

        ArrayList<Item> items = new ArrayList<Item>();

        for (int n = 1; n <= getItemCount(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null){
                items.add(item);
            }
        }

        endSession(session);
        return items;
    }
    
    /**
     * Get all text from database
     * 
     * @return items   ArrayList of text from database
     */
    public ArrayList<String> getAllText() {
        Session session = getSession();

        ArrayList<String> text = new ArrayList<String>();

        for (int n = 1; n <= getItemCount(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null){
                text.add(item.toString());
            }
        }

        if (text.isEmpty()) {
            logger.error("Could not retrieve text");
            text.add("Error: Could not retrieve text");
        }

        endSession(session);
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

        for (int n = 0; n < allText.size(); n++) {
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
        return "Error: Item could not be found";
    }
}
