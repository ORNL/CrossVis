package gov.ornl.csed.cda.experimental;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

/**
 * Created by csg on 9/16/16.
 */
public class BindingTest {

    public static void main (String args[]) {
        Parent parent = new Parent();

        parent.childProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("Child Changed");
            }
        });

        parent.childProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                System.out.println("Child Invalidated");
            }
        });

        parent.addValueToChild(1);

        parent.addValueToChild(2);

//        System.out.println(parent.getChild().getData().toString());

        parent.resetChild();
    }

    static class Parent {
        private SimpleObjectProperty<Child> child;

        public Parent() {
            child = new SimpleObjectProperty<>(new Child());
//            ObjectBinding binding = child.bind(child.get().dataProperty());
//            child.bind(child.get().dataProperty());
        }

        public SimpleObjectProperty childProperty() {
            return child;
        }

        public final Child getChild() {
            return child.get();
        }

        public void addValueToChild( int value) {
            getChild().addValue(value);
        }

        public void setValue(int value) {
            getChild().updateValue(value);
        }

        public void resetChild() {
            childProperty().set(new Child());
        }
    }

    static class Child {
        private ListProperty<TestObject> data;

        public Child() {
            data = new SimpleListProperty<TestObject>(FXCollections.observableArrayList());
            dataProperty().addListener(new ListChangeListener<TestObject>() {
                @Override
                public void onChanged(Change<? extends TestObject> c) {
                    System.out.println("List changed");
                }
            });
        }

//        public ObservableList<Integer> getData() { return data.get(); }

        public ListProperty<TestObject> dataProperty() { return data; }

        public void addValue(int value) {
            TestObject valueObject = new TestObject();
            valueObject.test = value;
            data.add(valueObject);
        }

        public void updateValue(int value) {
            data.get(0).test = value;
        }
    }

    static class TestObject {
        public int test;
    }
}
