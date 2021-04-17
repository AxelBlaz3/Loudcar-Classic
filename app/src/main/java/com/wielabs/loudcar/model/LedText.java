package com.wielabs.loudcar.model;

import java.util.Calendar;
import java.util.Date;

public class LedText {
    private int header;
    private int id;
    private Date beginDate;
    private Date endDate;
    private int priority;
    private int frequency;
    private int playCount;
    private int animation;
    private int edging;
    private boolean flash;
    private int implant;
    private int alignment;
    private int fontName;
    private int fontSize;
    private int speed;
    private String text;
    private int footer;

    public LedText(int id, Date startDate, Date endDate, int priority, int frequency, int playCount, int animation, int edging, boolean flash, int implant, int alignment, int fontName, int fontSize, int speed, String text) {
        this.id = id;
        this.beginDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.frequency = frequency;
        this.playCount = playCount;
        this.animation = animation;
        this.edging = edging;
        this.flash = flash;
        this.implant = implant;
        this.alignment = alignment;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.speed = speed;
        this.text = text;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getAnimation() {
        return animation;
    }

    public void setAnimation(int animation) {
        this.animation = animation;
    }

    public int getEdging() {
        return edging;
    }

    public void setEdging(int edging) {
        this.edging = edging;
    }

    public boolean isFlash() {
        return flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    public int getImplant() {
        return implant;
    }

    public void setImplant(int implant) {
        this.implant = implant;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getFontName() {
        return fontName;
    }

    public void setFontName(int fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getFooter() {
        return footer;
    }

    public void setFooter(int footer) {
        this.footer = footer;
    }

    public static LedText getDefaultLedTextForId(int id) {
        return new LedText(id, new Date(), getDateForTomorrow(), 1, 1, 1, 1, 0, false, 1, 1, 1,1, 1, "");
    }

    private static Date getDateForTomorrow() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }
}
