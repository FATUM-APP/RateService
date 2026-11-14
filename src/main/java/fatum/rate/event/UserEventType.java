package fatum.rate.event;

public final class UserEventType {

    public static final String SOURCE = "fatum.userservice";
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_BECAME_PROFESSIONAL = "USER_BECAME_PROFESSIONAL";
    public static final String PROFESSIONAL_BECAME_CLIENT = "PROFESSIONAL_BECAME_CLIENT";

    private UserEventType() {
    }
}
