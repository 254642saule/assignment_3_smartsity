package smartcity.bench;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
public final class SvgPlotWriter {
    public record Point(String algorithm, int vertices, int edges, double averageNanos) {}
    private static final Map<String, String> COLORS = Map.of(
            "Kruskal", "#2563eb", "Prim", "#dc2626", "SCC (Kosaraju)", "#7c3aed",
            "Topological (DAG)", "#059669", "Topological (cyclic)", "#d97706", "Dijkstra", "#0891
            b2");
    private SvgPlotWriter() {}
    public static void write(Path path, String title, String xLabel, boolean useEdges, List<Point
            > points) throws IOException {
        Files.createDirectories(path.getParent());
        int width = 980, height = 560, left = 96, right = 245, top = 76, bottom = 82;
        int plotWidth = width - left - right, plotHeight = height - top - bottom;
        double maxX = points.stream().mapToDouble(p -> Math.log10(useEdges ? Math.max(1, p.edges
                ()) : Math.max(1, p.vertices()))).max().orElse(1);
        double minX = points.stream().mapToDouble(p -> Math.log10(useEdges ? Math.max(1, p.edges
                ()) : Math.max(1, p.vertices()))).min().orElse(0);
        double maxY = points.stream().mapToDouble(Point::averageNanos).max().orElse(1);
        if (maxY <= 0) maxY = 1;
        double yTop = maxY * 1.1;
        Map<String, List<Point>> series = new LinkedHashMap<>();
        for (Point p : points) series.computeIfAbsent(p.algorithm(), ignored -> new ArrayList<>()
        ).add(p);
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\"
                height=\"").append(height).append("\" viewBox=\"0 0 ").append(width).append(' ').append(
                height).append("\">\n");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        svg.append(text(width / 2, 34, title, 22, "middle", "#111827", "bold"));
        for (int tick = 0; tick <= 5; tick++) {
            double yValue = yTop * tick / 5.0;
            double y = top + plotHeight - plotHeight * tick / 5.0;
            svg.append("<line x1=\"").append(left).append("\" y1=\"").append(y).append("\" x2=\""
            ).append(left + plotWidth).append("\" y2=\"").append(y).append("\" stroke=\"#e5e7eb\"/>\n");
            svg.append(text(left - 10, y + 4, String.format("%.0f", yValue), 12, "end", "#4b5563"
                    , "normal"));
        }
        TreeSet<Integer> xTicks = new TreeSet<>();
        for (Point p : points) xTicks.add(useEdges ? p.edges() : p.vertices());
        for (int tick : xTicks) {
            double raw = Math.max(1, tick);
            double x = left + (maxX == minX ? plotWidth / 2.0 : (Math.log10(raw) - minX) / (maxX
                    - minX) * plotWidth);
            String label = Integer.toString((int) raw);
            svg.append("<line x1=\"").append(x).append("\" y1=\"").append(top).append("\" x2=\"")
                    .append(x).append("\" y2=\"").append(top + plotHeight).append("\" stroke=\"#f3f4f6\"/>\n");
            svg.append(text(x, top + plotHeight + 22, label, 12, "middle", "#4b5563", "normal"));
        }
        svg.append("<line x1=\"").append(left).append("\" y1=\"").append(top + plotHeight).append
                ("\" x2=\"").append(left + plotWidth).append("\" y2=\"").append(top + plotHeight).append("\"
                stroke=\"#374151\"/>\n");
        svg.append("<line x1=\"").append(left).append("\" y1=\"").append(top).append("\" x2=\"").
                append(left).append("\" y2=\"").append(top + plotHeight).append("\" stroke=\"#374151\"/>\n");
        svg.append(text(left + plotWidth / 2, height - 28, xLabel + " (log scale)", 14, "middle",
                "#111827", "normal"));
        svg.append("<text x=\"24\" y=\"").append(top + plotHeight / 2).append("\" transform=\"
                rotate(-90 24 ").append(top + plotHeight / 2).append(" )\" font-family=\"Arial,sans-serif\"
                font-size=\"14\" fill=\"#111827\" text-anchor=\"middle\">Average execution time (ns)</text>\n
                ");
        int legendY = top + 14;
        for (Map.Entry<String, List<Point>> entry : series.entrySet()) {
            String color = COLORS.getOrDefault(entry.getKey(), "#4b5563");
            List<Point> line = entry.getValue().stream().sorted((a, b) -> Integer.compare(
                    useEdges ? a.edges() : a.vertices(), useEdges ? b.edges() : b.vertices())).toList();
            StringBuilder coords = new StringBuilder();
            for (Point p : line) {
                double raw = useEdges ? Math.max(1, p.edges()) : Math.max(1, p.vertices());
                double x = left + (maxX == minX ? plotWidth / 2.0 : (Math.log10(raw) - minX) / (
                        maxX - minX) * plotWidth);
                double y = top + plotHeight - (p.averageNanos() / yTop) * plotHeight;
                if (coords.length() > 0) coords.append(' ');
                coords.append(String.format("%.1f,%.1f", x, y));
                svg.append("<circle cx=\"").append(String.format("%.1f", x)).append("\" cy=\"").
                        append(String.format("%.1f", y)).append("\" r=\"4\" fill=\"").append(color).append("\"/>\n");
            }
            svg.append("<polyline points=\"").append(coords).append("\" fill=\"none\" stroke=\"")
                    .append(color).append("\" stroke-width=\"2.5\"/>\n");
            svg.append("<line x1=\"").append(left + plotWidth + 20).append("\" y1=\"").append(
                            legendY).append("\" x2=\"").append(left + plotWidth + 42).append("\" y2=\"").append(legendY).
                    append("\" stroke=\"").append(color).append("\" stroke-width=\"3\"/>\n");
            svg.append(text(left + plotWidth + 50, legendY + 4, entry.getKey(), 13, "start", "
                    #374151", "normal"));
                    legendY += 25;
        }
        svg.append("</svg>\n");
        Files.writeString(path, svg.toString());
    }
    private static String text(double x, double y, String value, int size, String anchor, String
            color, String weight) {
        return "<text x=\"" + x + "\" y=\"" + y + "\" text-anchor=\"" + anchor + "\" font-family
                =\"Arial,sans-serif\" font-size=\"" + size + "\" font-weight=\"" + weight + "\" fill=\"" +
                color + "\">" + escape(value) + "</text>\n";
    }
    private static String escape(String value) { return value.replace("&", "&amp;").replace("<",
            "&lt;").replace(">", "&gt;"); }
}