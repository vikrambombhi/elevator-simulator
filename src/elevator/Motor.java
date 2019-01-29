package elevator;

class Motor {
  public enum Direction { UP, DOWN, STOPPED };

  private Direction state;

  Motor() {
    state = Direction.STOPPED;
  }


  public Direction getDirection() {
    return state;
  }

  public void move(Direction d) { state = d; }
  public void stop() { state = Direction.STOPPED; }
}
