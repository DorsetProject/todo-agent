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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "items")
public class Item implements java.io.Serializable {

    private int itemId;
    private int listNumber;
    private String task;
    private String dateCreated;
    private String timeCreated;

    /**
     * Create an empty Item for a database
     */
    public Item() {
    }


    /**
     * Create an Item for a database with the given values
     *
     * 
     * @param listNumber  the item list number
     * @param task  the task
     * @param dateCreated  the date the item was created
     * @param timeCreated  the time the item was created
     */
    public Item(int listNumber, String task, String dateCreated,
                    String timeCreated) {
        this.listNumber = listNumber;
        this.task = task;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
    }

    @Id
    @GeneratedValue
    @Column(name = "item_id", unique = true,  nullable = false)
    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    @Column(name = "list_number", nullable = false)
    public int getListNumber() {
        return this.listNumber;
    }

    public void setListNumber(int listNumber) {
        this.listNumber = listNumber;
    }

    @Column(name = "task", nullable = false, length = 50)
    public String getTask() {
        return this.task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Column(name = "date_created", nullable = false, length = 10)
    public String getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Column(name = "time_created", nullable = false, length = 10)
    public String getTimeCreated() {
        return this.timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
    
    public String toString() {
        return listNumber + ")," + dateCreated + "," + timeCreated + "," + task;
    }

}
