// Production implementation. Depends on the OrderRepository abstraction, not the other way.
public class MySQLOrderRepository implements OrderRepository {

    @Override
    public void save(Order order) {
        System.out.println("MySQL: saved order ID " + order.getId()
                + " (total=$" + order.getTotal() + ")");
    }
}
