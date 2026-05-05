package com.example.wizard;

import com.example.HomeView;
import com.example.WizardLayout;
import com.example.data.Order;
import com.example.service.OrderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "payment-information", layout = WizardLayout.class)
@PermitAll
public class PaymentInformationSection extends Section {

    private TextField cardHolder, cardNumber, securityCode;
    private Select<String> expirationMonth, expirationYear;

    private Binder<Order> binder;

    @Autowired
    private transient OrderService service;


    public PaymentInformationSection() {
        System.out.println("Displaying payment information section");
        createLayout();
        initBinder();
    }

    private void createLayout() {
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Margin.Bottom.XLARGE, LumoUtility.Margin.Top.MEDIUM);

        H3 header = new H3("Personal details");
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.FontSize.XXLARGE);

        cardHolder = new TextField("Cardholder name");
        cardHolder.setPattern("[\\p{L} \\-]+");
        cardHolder.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        Div subSection = new Div();
        subSection.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM, LumoUtility.Margin.Bottom.SMALL);

        cardNumber = new TextField("Card Number");
        cardNumber.setPattern("[\\d ]{12,23}");
        cardNumber.addClassNames(LumoUtility.Flex.GROW, LumoUtility.Margin.Bottom.SMALL);

        securityCode = new TextField("Security Code");
        securityCode.setPattern("[0-9]{3,4}");

        expirationMonth = new Select<>();
        expirationMonth.setLabel("Expiration month");
        expirationMonth.setItems("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");

        expirationYear = new Select<>();
        expirationYear.setLabel("Expiration year");
        expirationYear.setItems("22", "23", "24", "25", "26");

        subSection.add(expirationMonth, expirationYear, securityCode);

        Button next = new Button("Complete");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(evt -> save());

        Button back = new Button("Back");
        back.addClickListener(evt -> getUI().ifPresent(ui -> ui.navigate(ShippingAddressSection.class)));

        add(header, cardHolder, cardNumber, subSection, new HorizontalLayout(back, next));
    }

    private void initBinder() {
        binder = new Binder<>();
        binder.forField(cardHolder).asRequired().bind(Order::getCardholderName, Order::setCardholderName);
        binder.forField(cardNumber).asRequired().bind(Order::getCardNo, Order::setCardNo);
        binder.forField(securityCode).asRequired().bind(Order::getSecurityCode, Order::setSecurityCode);
        binder.forField(expirationMonth).asRequired().bind(Order::getExpMonth, Order::setExpMonth);
        binder.forField(expirationYear).asRequired().bind(Order::getExpYear, Order::setExpYear);
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
            service.complete(order);
            getUI().ifPresent(ui -> {
                ui.navigate(HomeView.class);
            });
        }
    }


}
