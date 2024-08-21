package nl.medtechchain.chaincode.service.query;

import com.google.privacy.differentialprivacy.LaplaceNoise;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryResult;
import org.hyperledger.fabric.contract.Context;

import static nl.medtechchain.chaincode.util.ChaincodeResponseUtil.invalidTransaction;
import static nl.medtechchain.chaincode.util.ConfigUtil.differentialPrivacyConfig;

public class QueryService {

    public static QueryResult executeQuery(Query query) {
        var result = QueryResult.newBuilder().setError(invalidTransaction("Unknown query type").getError()).build();
        switch (query.getQueryType()) {
            case QUERY_TYPE_COUNT:
                result = count(ctx, tx);
                break;
            case QUERY_TYPE_GROUPED_COUNT:
                result = groupedCount(ctx, tx);
                break;
            case QUERY_TYPE_AVERAGE:
                result = average(ctx, tx);
                break;
        }

    }

    // TODO: Handle encryption
    private QueryResult count(Context ctx, Query tx) {
        var assets = getFilteredData(ctx, tx);
        var resultCount = assets.size();
        var differentialPrivacyConfig = differentialPrivacyConfig();
        switch (differentialPrivacyConfig.getMechanismCase()) {
            case LAPLACE:
                var laplaceConfig = differentialPrivacyConfig.getLaplace();
                resultCount = Math.abs((int) new LaplaceNoise().addNoise(resultCount, computeL1Sensitivity(tx), laplaceConfig.getEpsilon(), 0));
                break;
        }
        return QueryResult.newBuilder().setCountResult(resultCount).build();
    }

    // TODO: Handle encryption
    private QueryResult groupedCount(Context ctx, Query tx) {
        return null;
    }

    // TODO: Handle encryption
    private QueryResult average(Context ctx, Query tx) {
        return null;
    }

    // Sensitivity depends on the queried type and queried data
    // For now, its value will be defaulted to 1
    private long computeL1Sensitivity(Query tx) {
        return 1;
    }
}
