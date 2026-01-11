package com.example.nutrimap.util;

import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.domain.model.Visit;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static mock data for users, children, and visits.
 * In production, this would be replaced with database/API calls.
 */
public class StaticDataProvider {

    private static StaticDataProvider instance;

    private final List<User> users;
    private final List<Child> children;
    private final List<Visit> visits;

    private int nextUserId = 100;
    private int nextChildId = 100;
    private int nextVisitId = 100;

    private StaticDataProvider() {
        users = createMockUsers();
        children = createMockChildren();
        visits = createMockVisits();
    }

    public static synchronized StaticDataProvider getInstance() {
        if (instance == null) {
            instance = new StaticDataProvider();
        }
        return instance;
    }

    // ==================== USERS ====================

    private List<User> createMockUsers() {
        List<User> list = new ArrayList<>();
        list.add(new User(1, "Admin User", "admin@nutrimap.com", "admin123", "ADMIN", "", null));
        list.add(new User(2, "Rafiq Hasan", "rafiq.hasan@example.com", "pass@2025", "USER", "", "1"));
        list.add(new User(3, "Ayesha Rahman", "ayesha.rahman@example.com", "ayesha123", "USER", "", "1"));
        list.add(new User(4, "Sakib Hossain", "sakib.hossain@example.com", "sakib@bd", "USER", "", "2"));
        list.add(new User(5, "Nusrat Jahan", "nusrat.jahan@example.com", "nusrat456", "USER", "", "3"));
        list.add(new User(6, "Imran Ahmed", "imran.ahmed@example.com", "imran789", "USER", "", "100"));
        list.add(new User(7, "Sharmin Akter", "sharmin.akter@example.com", "sharmin@1", "USER", "", "50"));
        list.add(new User(8, "Mehedi Hasan", "mehedi.hasan@example.com", "mehedi2025", "USER", "", "55"));
        return list;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public User getUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User addUser(User user) {
        user.setId(nextUserId++);
        users.add(user);
        return user;
    }

    // ==================== CHILDREN ====================

    private List<Child> createMockChildren() {
        List<Child> list = new ArrayList<>();
        // Chattagram Division (ID: 1), Comilla District (ID: 1), Debidwar Upazila (ID: 1)
        list.add(new Child(1, "Arif Rahman", "Kamal Rahman", "Fatima Rahman", "01712345678",
                "Chattogram", "Comilla", "Debidwar", "Union 1", "1", "Main Branch", "",
                "Male", "2022-03-15", "1", "1", "1", "1"));
        list.add(new Child(2, "Fatima Akter", "Jobbar Akter", "Hasina Akter", "01812345679",
                "Chattogram", "Comilla", "Debidwar", "Union 2", "1", "Main Branch", "",
                "Female", "2021-08-20", "1", "1", "1", "2"));
        list.add(new Child(3, "Rifat Hasan", "Belal Hasan", "Sumaya Hasan", "01912345680",
                "Chattogram", "Comilla", "Debidwar", "Union 16", "2", "Main Branch", "",
                "Male", "2023-01-10", "1", "1", "2", "16"));
        list.add(new Child(4, "Nadia Islam", "Raju Islam", "Roksana Islam", "01612345681",
                "Chattogram", "Comilla", "Burichang", "Union 31", "3", "Main Branch", "",
                "Female", "2022-06-05", "1", "1", "3", "31"));
        // Dhaka Division (ID: 6)
        list.add(new Child(5, "Imran Khan", "Aziz Khan", "Rokeya Khan", "01512345682",
                "Dhaka", "Dhaka", "Dhamrai", "Union 2900", "100", "Dhaka Branch", "",
                "Male", "2021-11-25", "6", "47", "339", "2900"));
        list.add(new Child(6, "Sadia Begum", "Hanif Begum", "Nasima Begum", "01412345683",
                "Dhaka", "Dhaka", "Dhamrai", "Union 2901", "100", "Dhaka Branch", "",
                "Female", "2022-09-12", "6", "47", "339", "2901"));
        list.add(new Child(7, "Tanvir Ahmed", "Rafiq Ahmed", "Kulsum Ahmed", "01312345684",
                "Dhaka", "Dhaka", "Savar", "Union 2910", "101", "Dhaka Branch", "",
                "Male", "2023-02-28", "6", "47", "340", "2910"));
        // Rajshahi Division (ID: 2)
        list.add(new Child(8, "Mitu Khatun", "Sohel Khatun", "Amena Khatun", "01112345685",
                "Rajshahi", "Bogra", "Sariakandi", "Union 800", "50", "Rajshahi Branch", "",
                "Female", "2021-05-18", "2", "12", "103", "800"));
        list.add(new Child(9, "Rasel Mia", "Jashim Mia", "Bilkis Mia", "01912345686",
                "Rajshahi", "Bogra", "Sherpur", "Union 950", "55", "Rajshahi Branch", "",
                "Male", "2022-12-08", "2", "14", "120", "950"));
        list.add(new Child(10, "Shila Rani", "Mokhles Rani", "Josna Rani", "01712345687",
                "Rajshahi", "Naogaon", "Porsha", "Union 1050", "60", "Rajshahi Branch", "",
                "Female", "2023-04-22", "2", "15", "130", "1050"));
        return list;
    }

    public List<Child> getChildren() {
        return new ArrayList<>(children);
    }

    public Child getChildById(int id) {
        for (Child c : children) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public Child addChild(Child child) {
        child.setId(nextChildId++);
        children.add(child);
        return child;
    }

    public void updateChild(Child updated) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId() == updated.getId()) {
                children.set(i, updated);
                return;
            }
        }
    }

    public void deleteChild(int id) {
        children.removeIf(c -> c.getId() == id);
        // Also remove associated visits
        visits.removeIf(v -> v.getChildId() == id);
    }

    // ==================== VISITS ====================

    private List<Visit> createMockVisits() {
        List<Visit> list = new ArrayList<>();
        // Child 1 visits
        list.add(new Visit(1, 1, "2024-01-15", 8.5, 72.0, 130, "Healthy checkup", "Low", null, null, 1, false));
        list.add(new Visit(2, 1, "2024-04-20", 9.2, 76.0, 135, "Growing well", "Low", null, null, 1, false));
        list.add(new Visit(3, 1, "2024-07-10", 9.8, 80.0, 140, "", "Low", null, null, 1, false));
        // Child 2 visits - some concern
        list.add(new Visit(4, 2, "2024-02-05", 7.0, 68.0, 118, "Moderate concern", "Medium", null, null, 1, false));
        list.add(new Visit(5, 2, "2024-05-12", 7.5, 71.0, 122, "Slight improvement", "Medium", null, null, 1, false));
        list.add(new Visit(6, 2, "2024-08-18", 8.0, 74.0, 126, "Recovered well", "Low", null, null, 1, false));
        // Child 3 visits - high risk case
        list.add(new Visit(7, 3, "2024-03-01", 5.5, 60.0, 108, "SAM detected", "High", null, null, 1, false));
        list.add(new Visit(8, 3, "2024-04-15", 6.0, 62.0, 112, "Under treatment", "High", null, null, 1, false));
        list.add(new Visit(9, 3, "2024-06-01", 6.8, 65.0, 118, "Improving", "Medium", null, null, 1, false));
        // Child 4 visits
        list.add(new Visit(10, 4, "2024-01-20", 9.0, 74.0, 138, "", "Low", null, null, 1, false));
        list.add(new Visit(11, 4, "2024-05-25", 9.8, 78.0, 142, "", "Low", null, null, 1, false));
        // Child 5 visits - moderate risk
        list.add(new Visit(12, 5, "2024-02-10", 10.0, 82.0, 120, "MAM detected", "Medium", null, null, 1, false));
        list.add(new Visit(13, 5, "2024-06-15", 10.5, 85.0, 128, "Better now", "Low", null, null, 1, false));
        // Child 6 visits
        list.add(new Visit(14, 6, "2024-03-08", 8.2, 70.0, 132, "", "Low", null, null, 1, false));
        list.add(new Visit(15, 6, "2024-07-20", 9.0, 75.0, 138, "", "Low", null, null, 1, false));
        // Child 7 visits
        list.add(new Visit(16, 7, "2024-04-05", 6.0, 58.0, 145, "", "Low", null, null, 1, false));
        // Child 8 visits - high risk
        list.add(new Visit(17, 8, "2024-01-25", 8.0, 75.0, 110, "Severe case", "High", null, null, 1, false));
        list.add(new Visit(18, 8, "2024-03-30", 8.5, 77.0, 115, "Ongoing treatment", "High", null, null, 1, false));
        list.add(new Visit(19, 8, "2024-06-10", 9.2, 80.0, 123, "Recovering", "Medium", null, null, 1, false));
        // Child 9 visits
        list.add(new Visit(20, 9, "2024-02-18", 9.5, 78.0, 140, "", "Low", null, null, 1, false));
        list.add(new Visit(21, 9, "2024-08-05", 10.2, 83.0, 145, "", "Low", null, null, 1, false));
        // Child 10 visits - moderate risk
        list.add(new Visit(22, 10, "2024-05-01", 5.8, 55.0, 118, "MAM noted", "Medium", null, null, 1, false));
        list.add(new Visit(23, 10, "2024-08-15", 6.5, 60.0, 125, "Improving", "Medium", null, null, 1, false));
        return list;
    }

    public List<Visit> getVisits() {
        return new ArrayList<>(visits);
    }

    public List<Visit> getVisitsForChild(int childId) {
        List<Visit> result = new ArrayList<>();
        for (Visit v : visits) {
            if (v.getChildId() == childId) {
                result.add(v);
            }
        }
        return result;
    }

    public Visit getLatestVisitForChild(int childId) {
        Visit latest = null;
        for (Visit v : visits) {
            if (v.getChildId() == childId) {
                if (latest == null || v.getVisitDate().compareTo(latest.getVisitDate()) > 0) {
                    latest = v;
                }
            }
        }
        return latest;
    }

    public Visit addVisit(Visit visit) {
        visit.setId(nextVisitId++);
        visits.add(visit);
        return visit;
    }

    public void updateVisit(Visit updated) {
        for (int i = 0; i < visits.size(); i++) {
            if (visits.get(i).getId() == updated.getId()) {
                visits.set(i, updated);
                return;
            }
        }
    }
}
