package at.genetic.culture.schoolbus;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.beans.Transient;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jenes.AlgorithmEventListener;
import jenes.GenerationEventListener;
import jenes.GeneticAlgorithm;
import jenes.chromosome.Chromosome;
import jenes.population.Individual;
import jenes.population.Population;
import jenes.population.Population.Statistics;
import jenes.population.Population.Statistics.Group;
import jenes.tutorials.utils.Utils;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StatisticListner<T extends Chromosome<?>> implements
		GenerationEventListener<T>, AlgorithmEventListener<T> {

	private SchoolArea area;
	private MapPanel mp;
	private XYSeries min;
	private XYSeries mean;

	public StatisticListner(SchoolArea area) {
		this.area = area;
		openGui();
	}

	private void openGui() {
		JFrame frame = new JFrame("School Bus Culture – Map");
		JPanel p = new JPanel();
		BoxLayout layout = new BoxLayout(p, BoxLayout.PAGE_AXIS);
		p.setLayout(layout);

		mp = new MapPanel(area);
		p.add(mp);

		frame.add(p, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		JFrame frame2 = new JFrame("School Bus Culture – Stats");
		JPanel p2 = new JPanel();
		BoxLayout layout2 = new BoxLayout(p, BoxLayout.PAGE_AXIS);
		p.setLayout(layout2);

		p2.add(getStatPanel());

		frame2.add(p2, BorderLayout.CENTER);

		frame2.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		frame2.pack();
		frame2.setVisible(true);
	}

	@Override
	public void onAlgorithmStart(GeneticAlgorithm<T> ga, long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAlgorithmStop(GeneticAlgorithm<T> ga, long time) {

		Population.Statistics<?> stats = ga.getCurrentPopulation()
				.getStatistics();
		GeneticAlgorithm.Statistics algostats = ga.getStatistics();

		Group<?> legals = stats.getGroup(Population.LEGALS);

		Individual<?> solution = legals.get(0);
		Integer[] path = (Integer[]) solution.getChromosome().toArray();
		mp.addPath(path);
		
		System.out.println("Solution: ");
		System.out.println(solution.toCompleteString());
		System.out.format("found in %d ms.\n", algostats.getExecutionTime());
		System.out.println();

		Utils.printStatistics(stats);
	}

	@Override
	public void onAlgorithmInit(GeneticAlgorithm<T> ga, long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGeneration(GeneticAlgorithm<T> ga, long time) {
		Statistics<?> stat = ga.getCurrentPopulation().getStatistics();
		Group<?> legals = stat.getGroup(Population.LEGALS);
		System.out.println("Generation: " + ga.getGeneration() + "\tbest: "
				+ legals.getMin()[0] + "\tavg: " + legals.getMean()[0]);

		min.add(ga.getGeneration(), legals.getMin()[0]);
		mean.add(ga.getGeneration(), legals.getMean()[0]);
	}

	public class MapPanel extends JPanel {
		private static final long serialVersionUID = 3927006095992144643L;
		private SchoolArea area;
		private int w = 800;
		private int h = 800;
		private BusStop[] route = null;

		public MapPanel(SchoolArea area) {
			this.area = area;
		}

		@Override
		@Transient
		public Dimension getPreferredSize() {
			return new Dimension(w + 40, h + 40);
		}
		
		public void addPath(Integer[] path) {
			route = area.getRoute(path);
			repaint();
		}

		public void paint(Graphics g) {

			BusStop[] stops = area.getAllStops();
			int[] max = getMax(stops);
			Graphics2D g2 = (Graphics2D) g;

			Point p;
			g2.setPaint(Color.BLUE);
			for (int i = 1; i < stops.length; i++) {
				p = project(stops[i], max[0], max[1], max[2]);
				int size = (int) Math.sqrt(((double)(32*stops[i].numberOfPupils))/Math.PI);
				g2.fillOval(p.x-size/2, p.y-size/2, size, size);
			}

			Point p2;
		    g2.setStroke(new BasicStroke(2));
			int c = -1;
			Color[] color = new Color[] {Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.GRAY,  Color.PINK, Color.YELLOW, Color.RED};
			for (int i = 1; i < route.length; i++) {
				if (route[i-1] == area.school) {
					c = (c+1)%color.length;
					g2.setPaint(color[c]);
				}
				p = project(route[i-1], max[0], max[1], max[2]);
				p2 = project(route[i], max[0], max[1], max[2]);
				g2.drawLine(p.x, p.y, p2.x, p2.y);
			}

			p = project(stops[0], max[0], max[1], max[2]);
			g2.setPaint(Color.RED);
			g2.fillOval(p.x-5, p.y-5, 10, 10);
		}

		private int[] getMax(BusStop[] stops) {
			int maxX = 0, minX = 0, maxY = 0, minY = 0;
			for (BusStop busStop : stops) {
				if (busStop.x > maxX)
					maxX = busStop.x;
				else if (busStop.x < minX)
					minX = busStop.x;
				if (busStop.y > maxY)
					maxY = busStop.y;
				else if (busStop.y < minY)
					minY = busStop.y;
			}
			int min = (minX < minY ? minX : minY);
			int max = (maxX > maxY ? maxX : maxY);
			int diff = max - min;

			int xoffset = minX + (maxX - minX) / 2;
			int yoffset = minY + (maxY - minY) / 2;

			return new int[] { diff, xoffset, yoffset };
		}

		private Point project(BusStop stop, int diff, int xoffset, int yoffset) {
			double scale = (double) w / (double) diff;
			double shift = ((double) w / 2.0) + 20;

			int x = (int) (((double) stop.x - (double) xoffset) * scale + shift);
			int y = (int) (((double) stop.y - (double) yoffset) * scale + shift);

			Point p = new Point(x, y);
			return p;
		}
	}

	public ChartPanel getStatPanel() {
		min = new XYSeries("min");
		mean = new XYSeries("mean");

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(min);
		dataset.addSeries(mean);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		NumberAxis xAxis = new NumberAxis("generation");
		NumberAxis yAxis = new NumberAxis("cost");
		yAxis.setAutoRangeIncludesZero(false);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		JFreeChart chart = new JFreeChart(plot);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 700));
		return chartPanel;
	}
}
