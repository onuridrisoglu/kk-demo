package com.example;

import com.example.component.Step;
import com.example.component.Stepper;
import com.example.wizard.PaymentInformationSection;
import com.example.wizard.PersonalDetailsSection;
import com.example.wizard.ShippingAddressSection;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;


@PageTitle("Wizard")
public class WizardLayout extends Main implements RouterLayout, AfterNavigationObserver {

    private Div content;

    public WizardLayout() {
        addClassNames("com.example.wizard-layout");
        add(createStepper(), createContent());
    }

    private Stepper createStepper() {
        Step personalDetails = new Step("Personal Details", PersonalDetailsSection.class);
        Step shippingAddress = new Step("Shipping Address", ShippingAddressSection.class);
        Step paymentInformation = new Step("Payment Information", PaymentInformationSection.class);

        Stepper stepper = new Stepper(personalDetails, shippingAddress, paymentInformation);
        stepper.addClassNames(BoxSizing.BORDER, MaxWidth.SCREEN_SMALL, Padding.LARGE);
        stepper.setOrientation(Stepper.Orientation.HORIZONTAL);
        stepper.setSmall(true);
        return stepper;
    }

    private Div createContent() {
        this.content = new Div();
        this.content.addClassNames(Flex.GROW, Overflow.AUTO);
        return this.content;
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            this.content.removeAll();
            this.content.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (event.getLocation().getPath().contains("personal-details")) {

        } else if (event.getLocation().getPath().contains("shipping-address")) {

        } else if (event.getLocation().getPath().contains("payment-information")) {
        } else {
        }
    }
}
