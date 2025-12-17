public abstract class WirelessPhone extends AbstractPhone {
    private int hour;
    public WirelessPhone(int year, int hour) {
        super(year);
        this.hour = hour;
    }

    public abstract void call(int outputNumber);

    public abstract void ring(int inputNumber);
}
