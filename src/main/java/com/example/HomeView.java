package com.example;

import com.example.wizard.PersonalDetailsSection;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home")
@Route("")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        addClassNames("centered-content");

        Button start = new Button("Create new Order");
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        start.addClickListener(evt -> getUI().ifPresent(ui -> {
            ComponentUtil.setData(ui, "order-id", null);
            ui.navigate(PersonalDetailsSection.class);
        }));

        add(start);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        System.out.println("Displaying home view");
        if (ComponentUtil.getData(attachEvent.getUI(), "order-id") != null) {
            addComponentAsFirst(new H3("Your order has sent successfully!"));
        }
    }
}
