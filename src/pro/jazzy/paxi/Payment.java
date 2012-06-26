
package pro.jazzy.paxi;

public class Payment implements RoadEvent {

    int distance;

    float amount;

    public Payment(float amount) {

        this.amount = amount;
    }

    @Override
    public int getDistance() {

        return distance;
    }

    @Override
    public void setDistance(int distance) {

        this.distance = distance;
    }

    public float getAmount() {

        return amount;
    }

}
