package nl.medtechchain.chaincode.service.solver;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ILPSolver {
    static {
        Loader.loadNativeLibraries();
    }

    public Optional<Map<String, Integer>> solveSystem(List<String> v, List<Integer> c, long s1, long s2) {
        assert v.size() == c.size();

        var solver = new MPSolver("ILPSolver", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

        var x = new MPVariable[v.size()];
        for (int i = 0; i < v.size(); i++) {
            x[i] = solver.makeIntVar(0, s2, "x" + (i + 1));
        }

        var weightedSumConstraint = solver.makeConstraint(s1, s1, "weighted_sum_constraint");
        var countSumConstraint = solver.makeConstraint(s2, s2, "count_sum_constraint");
        for (int i = 0; i < x.length; i++) {
            weightedSumConstraint.setCoefficient(x[i], c.get(i));
            countSumConstraint.setCoefficient(x[i], 1);
        }

        var objective = solver.objective();
        objective.setMinimization();

        var resultStatus = solver.solve();

        // Check if the problem has an optimal solution.
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            var result = new HashMap<String, Integer>();
            for (int i = 0; i < x.length; i++)
                result.put(v.get(i), (int) x[i].solutionValue());
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
