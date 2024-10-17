package org.genetic.utils.entities;

public record Node(double x, double y) {
    public double getDistance(Node otherNode, DistFormat format) {
        switch (format) {
            case EUC_2D -> {
                var distX = this.x - otherNode.x;
                var distY = this.y - otherNode.y;
                return nint(Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2)));
            }
            case GEO -> {
                var q1 = Math.cos(this.getLongitude() - otherNode.getLongitude());
                var q2 = Math.cos(this.getLatitude() - otherNode.getLatitude());
                var q3 = Math.cos(this.getLatitude() + otherNode.getLatitude());
                return (int) (6378.388 * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0);
            }
            default -> throw new UnsupportedOperationException("Unsupported format: " + format);
        }
    }

    private long nint(double val) {
        return Math.round(val + 0.5);
    }

    private double getLatitude() {
        var deg = nint(this.x);
        var min = this.x - deg;
        return Math.PI * (deg + 5.0 * min / 3.0) / 180.0;
    }

    private double getLongitude() {
        var deg = nint(this.y);
        var min = this.y - deg;
        return Math.PI * (deg + 5.0 * min / 3.0) / 180.0;
    }
}
