package com.gym.ui;

import com.gym.model.MembershipPlan;
import com.gym.model.Trainer;
import com.gym.service.GymService;

/** Loads a small set of demo records so the app can be tried straight away. */
public final class SampleData {

    private SampleData() {
    }

    public static void load(GymService service) {
        MembershipPlan basic = service.addPlan("Basic", 29.99, 1);
        MembershipPlan standard = service.addPlan("Standard", 49.99, 3);
        MembershipPlan premium = service.addPlan("Premium", 89.99, 12);

        Trainer arjun = service.addTrainer("Arjun Rao", "9000000001", "arjun@gym.com", "Strength");
        Trainer meera = service.addTrainer("Meera Iyer", "9000000002", "meera@gym.com", "Yoga & Mobility");

        String m1 = service.addMember("Ravi Kumar", "9111111111", "ravi@mail.com", basic.getId()).getId();
        String m2 = service.addMember("Anita Sharma", "9222222222", "anita@mail.com", premium.getId()).getId();
        String m3 = service.addMember("Suresh Babu", "9333333333", "suresh@mail.com", standard.getId()).getId();
        String m4 = service.addMember("Priya Nair", "9444444444", "priya@mail.com", premium.getId()).getId();
        service.addMember("Karthik Reddy", "9555555555", "karthik@mail.com", basic.getId());
        service.addMember("Divya Menon", "9666666666", "divya@mail.com", standard.getId());

        service.assignTrainer(m1, arjun.getId());
        service.assignTrainer(m2, meera.getId());
        service.assignTrainer(m3, arjun.getId());
        service.assignTrainer(m4, meera.getId());
    }
}
