package smartcity.bench;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public final class SvgPlotWriter {
    public record Point(
            String algorithm,
            int vertices,
            int edges,
            double averageNanos
    ) {}

    private static final Map<String, String> COLORS = Map.of(
            "Kruskal", "#2563eb",
            "Prim", "#dc2626",
            "SCC (Kosaraju)", "#7c3aed",
            "Topological (DAG)", "#059669",
            "Topological (cyclic)", "#d97706",
            "Dijkstra", "#0891b2"
    );

    private SvgPlotWriter() {}

    public static void write(
            Path path,
            String title,
            String xLabel,
            boolean useEdges,
            List<Point> points
    ) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        int width = 980;
        int height = 560;
        int left = 96;
        int right = 245;
        int top = 76;
        int bottom = 82;

        int plotWidth = width - left - right;
        int plotHeight = height - top - bottom;

        double minX = points.stream()
                .mapToDouble(point -> Math.log10(xValue(point, useEdges)))
                .min()
                .orElse(0);

        double maxX = points.stream()
                .mapToDouble(point -> Math.log10(xValue(point, useEdges)))
                .max()
                .orElse(1);

        double maxY = points.stream()
                .mapToDouble(Point::averageNanos)
                .max()
                .orElse(1);

        if (maxY <= 0) {
            maxY = 1;
        }

        double yTop = maxY * 1.1;

        Map<String, List<Point>> series = new LinkedHashMap<>();
        for (Point point : points) {
            series.computeIfAbsent(point.algorithm(), ignored -> new ArrayList<>())
                    .add(point);
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
                .append("width=\"").append(width).append("\" ")
                .append("height=\"").append(height).append("\" ")
                .append("viewBox=\"0 0 ").append(width).append(' ').append(height).append("\">\n");

        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        svg.append(text(width / 2.0, 34, title, 22, "middle", "#111827", "bold"));

        for (int tick = 0; tick <= 5; tick++) {
            double yValue = yTop * tick / 5.0;
            double y = top + plotHeight - plotHeight * tick / 5.0;

            svg.append("<line x1=\"").append(left)
                    .append("\" y1=\"").append(number(y))
                    .append("\" x2=\"").append(left + plotWidth)
                    .append("\" y2=\"").append(number(y))
                    .append("\" stroke=\"#e5e7eb\"/>\n");

            svg.append(text(
                    left - 10,
                    y + 4,
                    String.format(Locale.ROOT, "%.0f", yValue),
                    12,
                    "end",
                    "#4b5563",
                    "normal"
            ));
        }

        TreeSet<Integer> xTicks = new TreeSet<>();
        for (Point point : points) {
            xTicks.add(useEdges ? point.edges() : point.vertices());
        }

        for (int tick : xTicks) {
            double x = xCoordinate(tick, minX, maxX, left, plotWidth);

            svg.append("<line x1=\"").append(number(x))
                    .append("\" y1=\"").append(top)
                    .append("\" x2=\"").append(number(x))
                    .append("\" y2=\"").append(top + plotHeight)
                    .append("\" stroke=\"#f3f4f6\"/>\n");

            svg.append(text(
                    x,
                    top + plotHeight + 22,
                    Integer.toString(Math.max(1, tick)),
                    12,
                    "middle",
                    "#4b5563",
                    "normal"
            ));
        }

        svg.append("<line x1=\"").append(left)
                .append("\" y1=\"").append(top + plotHeight)
                .append("\" x2=\"").append(left + plotWidth)
                .append("\" y2=\"").append(top + plotHeight)
                .append("\" stroke=\"#374151\"/>\n");

        svg.append("<line x1=\"").append(left)
                .append("\" y1=\"").append(top)
                .append("\" x2=\"").append(left)
                .append("\" y2=\"").append(top + plotHeight)
                .append("\" stroke=\"#374151\"/>\n");

        svg.append(text(
                left + plotWidth / 2.0,
                height - 28,
                xLabel + " (log scale)",
                14,
                "middle",
                "#111827",
                "normal"
        ));

        double yAxisCenter = top + plotHeight / 2.0;
        svg.append("<text x=\"24\" y=\"").append(number(yAxisCenter))
                .append("\" transform=\"rotate(-90 24 ")
                .append(number(yAxisCenter))
                .append(")\" font-family=\"Arial,sans-serif\" font-size=\"14\" ")
                .append("fill=\"#111827\" text-anchor=\"middle\">")
                .append(escapeXml("Average execution time (ns)"))
                .append("</text>\n");

        int legendY = top + 14;

        for (Map.Entry<String, List<Point>> entry : series.entrySet()) {
            String algorithm = entry.getKey();
            String color = COLORS.getOrDefault(algorithm, "#4b5563");

            List<Point> line = entry.getValue();
            line.sort((first, second) -> Integer.compare(
                    useEdges ? first.edges() : first.vertices(),
                    useEdges ? second.edges() : second.vertices()
            ));

            StringBuilder coordinates = new StringBuilder();

            for (Point point : line) {
                int value = useEdges ? point.edges() : point.vertices();
                double x = xCoordinate(value, minX, maxX, left, plotWidth);
                double y = top + plotHeight
                        - (point.averageNanos() / yTop) * plotHeight;

                if (coordinates.length() > 0) {
                    coordinates.append(' ');
                }
                coordinates.append(number(x)).append(',').append(number(y));

                svg.append("<circle cx=\"").append(number(x))
                        .append("\" cy=\"").append(number(y))
                        .append("\" r=\"4\" fill=\"").append(color).append("\"/>\n");
            }

            svg.append("<polyline points=\"").append(coordinates)
                    .append("\" fill=\"none\" stroke=\"").append(color)
                    .append("\" stroke-width=\"2.5\"/>\n");

            svg.append("<line x1=\"").append(left + plotWidth + 20)
                    .append("\" y1=\"").append(legendY)
                    .append("\" x2=\"").append(left + plotWidth + 42)
                    .append("\" y2=\"").append(legendY)
                    .append("\" stroke=\"").append(color)
                    .append("\" stroke-width=\"3\"/>\n");

            svg.append(text(
                    left + plotWidth + 50,
                    legendY + 4,
                    algorithm,
                    13,
                    "start",
                    "#374151",
                    "normal"
            ));

            legendY += 25;
        }

        svg.append("</svg>\n");
        Files.writeString(path, svg.toString(), StandardCharsets.UTF_8);
    }

    private static double xValue(Point point, boolean useEdges) {
        int value = useEdges ? point.edges() : point.vertices();
        return Math.max(1, value);
    }

    private static double xCoordinate(
            int value,
            double minX,
            double maxX,
            int left,
            int plotWidth
    ) {
        double logValue = Math.log10(Math.max(1, value));

        if (maxX == minX) {
            return left + plotWidth / 2.0;
        }

        return left + (logValue - minX) / (maxX - minX) * plotWidth;
    }

    private static String text(
            double x,
            double y,
            String value,
            int size,
            String anchor,
            String color,
            String weight
    ) {
        return "<text x=\"" + number(x)
                + "\" y=\"" + number(y)
                + "\" text-anchor=\"" + anchor
                + "\" font-family=\"Arial,sans-serif\" font-size=\"" + size
                + "\" font-weight=\"" + weight
                + "\" fill=\"" + color + "\">"
                + escapeXml(value)
                + "</text>\n";
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}