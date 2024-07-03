
class Edge1{
    int source;
    int target;
    int distance;

    Edge1(int source, int target, int distance) {
        this.source = source;
        this.target = target;
        this.distance = distance;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge1 edge1 = (Edge1) o;

        if (source != edge1.source) return false;
        if (target != edge1.target) return false;
        return distance == edge1.distance;
    }

    @Override
    public int hashCode() {
        int result = source;
        result = 31 * result + target;
        result = 31 * result + distance;
        return result;
    }
}