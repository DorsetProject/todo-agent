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

    public Item() {
    }

    public Item(int listNumber, String task, String dateCreated,
                    String timeCreated) {
        //this.itemId = itemId;
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
