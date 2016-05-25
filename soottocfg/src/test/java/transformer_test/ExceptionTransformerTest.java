/**
 * 
 */
package transformer_test;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import soot.Body;
import soot.SootMethod;
import soot.jimple.toolkits.annotation.nullcheck.NullnessAnalysis;
import soot.toolkits.graph.CompleteUnitGraph;
import soottocfg.soot.transformers.ExceptionTransformer;

/**
 * @author schaef
 *
 */
@RunWith(Parameterized.class)
public class ExceptionTransformerTest extends AbstractTransformerTest {

		@Parameterized.Parameters(name = "{index}: check ({1})")
		public static Collection<Object[]> data() {
			List<Object[]> filenames = new LinkedList<Object[]>();
			final File source_dir = new File(testRoot + "transformation_tests/exceptions/");
			File[] directoryListing = source_dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					if (child.isFile() && child.getName().endsWith(".java")) {
						filenames.add(new Object[] { child, child.getName() });
					} 
				}
			} 
			return filenames;
		}

		public ExceptionTransformerTest(File source, String name) {
			this.sourceFile = source;
		}
		
		@Test
		public void test() {
			for (SootMethod sm : loadSootMethods()) {
				System.out.println("Transforming " + sm.getSignature());
				Body body = sm.retrieveActiveBody();
				ExceptionTransformer em = new ExceptionTransformer(
						new NullnessAnalysis(new CompleteUnitGraph(body)),
						false);
				em.transform(body);
				System.out.println(body);				
			}
		}
		

}
