package com.example.simulinkviewer;


import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Block {
    private int ID;
    private String name;
    private double right;
    private double left;
    private double up;
    private double down;
    private boolean mirror;
    private int inputsNum;
    private int outputsNum;
    private Label container;
    private Label lName;

    public Block(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror) {
        this.ID = ID;
        this.name = name;
        this.right = right;
        this.left = left;
        this.up = up;
        this.down = down;
        this.inputsNum = inputsNum;
        this.outputsNum = outputsNum;
        this.mirror = mirror;


        container = new Label();
        container.setId("LabelShape");
        container.setLayoutX(left);
        container.setLayoutY(up);
        container.setMinSize(right - left, down - up);


        lName = new Label(name);
        HBox h = new HBox();
        h.getChildren().add(lName);
        Scene s = new Scene(h);
        lName.applyCss();
        lName.setId("LabelName");
        lName.setLayoutX((left + (right - left) / 2.0) - (lName.prefWidth(-1) / 2.0));
        lName.setLayoutY(down);
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public double getLeft() {
        return left;
    }

    public double getUp() {
        return up;
    }

    public double getDown() {
        return down;
    }

    public double getWidth() {
        return (right - left);
    }

    public double getHeight() {
        return (down - up);
    }

    public int getInputsNum() {
        return inputsNum;
    }

    public int getOutputsNum() {
        return outputsNum;
    }

    public double getRight() {
        return right;
    }

    public boolean isMirror() {
        return mirror;
    }

    public Label getContainer() {
        return container;
    }

    public Label getlName() {
        return lName;
    }

    public void setInputsNum(int inputsNum) {
        this.inputsNum = inputsNum;
    }

    public void addBlock(Group root) {
        root.getChildren().addAll(container,lName);
    }
}

class Saturation extends Block {
    static Image image = new Image("saturate.png");
    ImageView imgview;


    public Saturation(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror) {
        super(ID, name, left, up, right, down, inputsNum, outputsNum, mirror);
        imgview = new ImageView(image);
    }

    @Override
    public void addBlock(Group root) {
        super.addBlock(root);
        imgview.setX(super.getLeft() + 4);
        imgview.setY(super.getUp() + 4);
        imgview.setFitWidth(getWidth() - 8);
        imgview.setFitHeight(getHeight() - 8);

        root.getChildren().add(imgview);
    }
}
class UnitDelay extends Block {
    static Image image = new Image("ud.png");
    ImageView imgview;


    public UnitDelay(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror) {
        super(ID, name, left, up, right, down, inputsNum, outputsNum, mirror);
        imgview = new ImageView(image);
    }

    @Override
    public void addBlock(Group root) {
        super.addBlock(root);
        imgview.setX(super.getLeft() + 4);
        imgview.setY(super.getUp() + 4);
        imgview.setFitWidth(getWidth() - 8);
        imgview.setFitHeight(getHeight() - 8);
        root.getChildren().add(imgview);
    }
}
class Scope extends Block {
    static Image image = new Image("scope.png");
    ImageView imgview;

    public Scope(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror) {
        super(ID, name, left, up, right, down, inputsNum, outputsNum, mirror);
        imgview = new ImageView(image);
    }

    @Override
    public void addBlock(Group root) {
        super.addBlock(root);
        imgview.setX(super.getLeft() + 4);
        imgview.setY(super.getUp() + 4);
        imgview.setFitWidth(getWidth() - 8);
        imgview.setFitHeight(getHeight() - 8);
        root.getChildren().add(imgview);
    }
}

class Add extends Block {
    ArrayList<Label>signLabels = new ArrayList<Label>();
    public Add(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror, String inputs) {
        super(ID, name, left, up, right, down, inputsNum, outputsNum, mirror);
        double y = 0;
        double amount = 24;
        if(inputs.length()==0) {
            StringBuilder inputsBuilder = new StringBuilder(inputs);
            for (int i = 0; i < inputsNum; i++)
                inputsBuilder.append('+');
            inputs = inputsBuilder.toString();
        }

        for (int i = 0; i < inputs.length(); i++) {
            Label l = new Label(String.valueOf(inputs.charAt(i)));
            l.setId("sign");
            l.setMinWidth(40);
            if (inputs.charAt(i) == '+')
                l.setLayoutX(left + 3);
            else
                l.setLayoutX(left + 5);

            l.setLayoutY(up + y);
            y += (amount / inputsNum);
            signLabels.add(l);
        }
    }
    @Override
    public void addBlock(Group root) {
        super.addBlock(root);
        for (Label signLabel : signLabels) root.getChildren().add(signLabel);
    }
}

class Constant extends Block {
    String  value;

    public Constant(int ID, String name, double left, double up, double right, double down, int inputsNum, int outputsNum, boolean mirror, String value) {
        super(ID, name, left, up, right, down, inputsNum, outputsNum, mirror);
        super.getContainer().setId("Constant");
        super.getContainer().setText(value);
        this.value = value;
    }
    @Override
    public void addBlock(Group root) {
        super.addBlock(root);
    }
}
