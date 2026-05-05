package com.example.wizard;

import com.example.HomeView;
import com.example.WizardLayout;
import com.example.data.Order;
import com.example.service.OrderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "personal-details", layout = WizardLayout.class)
@PermitAll
public class PersonalDetailsSection extends Section {

    private TextField name, phone;
    private EmailField email;
    private Binder<Order> binder;

    @Autowired
    private transient OrderService service;


    public PersonalDetailsSection() {
        System.out.println("Displaying personal details section");
        createLayout();
        initBinder();
    }

    private void createLayout() {
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Margin.Bottom.XLARGE, LumoUtility.Margin.Top.MEDIUM);

        H3 header = new H3("Personal details");
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.FontSize.XXLARGE);

        name = new TextField("Name");
        name.setRequiredIndicatorVisible(true);
        name.setPattern("[\\p{L} \\-]+");
        name.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        email = new EmailField("Email address");
        email.setRequiredIndicatorVisible(true);
        email.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        email.addValueChangeListener(evt -> {
            boolean isEmailAlreadyExist = service.isAlreadyExist(evt.getValue());
            email.setInvalid(isEmailAlreadyExist);
            email.setErrorMessage(isEmailAlreadyExist ? "Already exist" : "");
        });

        phone = new TextField("Phone number");
        phone.setRequiredIndicatorVisible(true);
        phone.setPattern("[\\d \\-\\+]+");
        phone.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        phone.setMaxLength(16);

        Button next = new Button("Next");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(evt -> save());

        Button cancel = new Button("Cancel");
        cancel.addClickListener(evt -> getUI().ifPresent(ui -> ui.navigate(HomeView.class)));

        add(header, name, email, phone, new HorizontalLayout(cancel, next));
    }

    private void initBinder() {
        binder = new Binder<>();
        binder.forField(name).asRequired().bind(Order::getName, Order::setName);
        binder.forField(phone).asRequired().bind(Order::getPhone, Order::setPhone);
        binder.forField(email).asRequired().bind(Order::getEmail, Order::setEmail);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Order order = new Order();
        Long orderId = (Long) ComponentUtil.getData(attachEvent.getUI(), "order-id");
        if (orderId != null) {
            order = service.getOrder(orderId);
        }
        binder.setBean(order);
    }

    private void save() {
        if (binder.validate().isOk()) {
            Order order = binder.getBean();
            order = service.save(order);
            ComponentUtil.setData(UI.getCurrent(), "order-id", order.getId());
            getUI().ifPresent(ui -> ui.navigate(ShippingAddressSection.class));
        }
    }
}
