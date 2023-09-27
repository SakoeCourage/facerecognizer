package application;

import javafx.event.Event;
import javafx.event.EventType;

public class NotifcationEvent extends Event  {

	  /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final EventType<NotifcationEvent> NOTIFICATION_EVENT_TYPE = new EventType<>(Event.ANY, "CUSTOM_EVENT");

		    private String eventData;

		    public NotifcationEvent(String eventData) {
		        super(NOTIFICATION_EVENT_TYPE);
		        this.eventData = eventData;
		    }

		    public String getEventData() {
		        return eventData;
		    }
}
