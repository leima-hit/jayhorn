package jayhorn.hornify;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jayhorn.Log;
import jayhorn.hornify.encoder.MethodEncoder;
import jayhorn.solver.Prover;
import jayhorn.solver.ProverFactory;
import jayhorn.solver.ProverFun;
import jayhorn.solver.ProverHornClause;
import soottocfg.cfg.Program;
import soottocfg.cfg.method.Method;

/**
 * Class to hornify Java program
 * 
 * @author teme
 *
 */

public class Hornify {

	private final ProverFactory factory;

	private Prover prover;

	public final List<ProverHornClause> clauses = new LinkedList<ProverHornClause>();

	public Hornify(ProverFactory fac) {
		this.factory = fac;	
	}

	/**
	 * Main method to encode into Horn
	 * @param program
	 */
	public HornEncoderContext toHorn(Program program){
		prover = factory.spawn();
		prover.setHornLogic(true);

		Log.info("Interprocedural Push/Pull Ordering");
		Log.info("Building type hierarchy ... ");
		Log.info("Generating Method Contract ... ");
		HornEncoderContext hornContext = new HornEncoderContext(prover, program);

		Log.info("Transform Program Methods into Horn Clauses ... ");

		for (Method method : program.getMethods()) {
			final MethodEncoder encoder = new MethodEncoder(prover, method, hornContext);
			clauses.addAll(encoder.encode());		
		}
		return hornContext;
	}

	/**
	 * Return the current prover object
	 * @return prover
	 */
	public Prover getProver() {
		return prover;
	}


	/**
	 * Write clauses
	 * @return
	 */
	public String writeHorn() {
		StringBuilder st = new StringBuilder();
		for (ProverHornClause clause : clauses)
			st.append("\t\t" + clause + "\n");
		st.append("\t\t-------------\n");
		return st.toString();
	}

	/**
	 * Write Horn clauses to file
	 */
	public static void hornToFile(List<ProverHornClause> clauses,
			int num) {
		// write Horn clauses to file
		String out = jayhorn.Options.v().getOutDir();
		if (out != null) {
			String basename = jayhorn.Options.v().getOutBasename();
			Path file = Paths.get(out + basename + "_" + num + ".horn");

			LinkedList<String> it = new LinkedList<String>();
			for (ProverHornClause clause : clauses)
				it.add("\t\t" + clause);

			writeToFile(file, it);
		}
	}

	/**
	 * Write Horn clauses to an SMT-LIB file
	 */
	public static void hornToSMTLIBFile(List<ProverHornClause> clauses,
			int num,
			Prover prover) {
		String out = jayhorn.Options.v().getOutDir();
		if (out != null) {
			String basename = jayhorn.Options.v().getOutBasename();
			Path file = Paths.get(out + basename + "_" + num + ".smt2");

			Log.info("Writing Horn clauses to " + file);

			LinkedList<String> it = new LinkedList<String>();

			it.add("(set-info :origin \"Horn clauses generated by JayHorn\")");
			it.add("(set-logic HORN)");

			it.add("");

			Set<ProverFun> predicates = new LinkedHashSet<ProverFun>();
			for (ProverHornClause clause : clauses) {
				// null indicates that the head of the clause is "false"
				if (clause.getHeadFun() != null)
					predicates.add(clause.getHeadFun());
				for (int i = 0; i < clause.getArity(); ++i)
					predicates.add(clause.getBodyFun(i));
			}

			for (ProverFun fun : predicates)
				it.add(prover.toSMTLIBDeclaration(fun));

			it.add("");

			for (ProverHornClause clause : clauses)
				it.add("(assert " + prover.toSMTLIBFormula(clause) + ")");

			it.add("");
			it.add("(check-sat)");

			writeToFile(file, it);
		}
	}

	private static void writeToFile(Path file, List<String> it) {
		try {					
			Path parent = file.getParent();
			if (parent != null)
				Files.createDirectories(parent);
			Files.write(file, it, Charset.forName("UTF-8"));
		} catch (Exception e) {
			System.err.println("Error writing file " + file);
		}
	}


}
