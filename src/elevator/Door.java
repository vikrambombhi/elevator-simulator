package elevator;

class Door {
  public enum Position { OPENED, CLOSED };

  private Position position;

  Door() {
    position = Position.CLOSED;
  }

  public Position getPosition() {
    return position;
  }

  public void open()  { position = Position.OPENED; }
  public void close() { position = Position.CLOSED; }
}
