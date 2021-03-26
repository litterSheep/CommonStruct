package com.ly.common.eventBus;

/**
 * Created by ly on 2017/9/9 20:28.
 */
public class EventObj {
    private EventBusFlag eventBusFlag;
    private Object obj;

    public EventObj(EventBusFlag eventBusFlag) {
        this.eventBusFlag = eventBusFlag;
    }

    public EventObj(EventBusFlag eventBusFlag, Object obj) {
        this.eventBusFlag = eventBusFlag;
        this.obj = obj;
    }

    public EventBusFlag getEventBusFlag() {
        return eventBusFlag;
    }

    public void setEventBusFlag(EventBusFlag eventBusFlag) {
        this.eventBusFlag = eventBusFlag;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
