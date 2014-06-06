package org.openforis.collect.android.util;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class CoordinateUtils {
    private static final CRSFactory CRS_FACTORY = createCrsFactory();

    public static double[] transform(String fromCrs, double[] coord, String toCrs) {
        List<CoordinateOperation> coordOps = CoordinateOperationFactory.createCoordinateOperations(createCrs(fromCrs), createCrs(toCrs));
        double[] result = coord;
        if (!coordOps.isEmpty())
            for (CoordinateOperation op : coordOps)
                try {
                    result = op.transform(result);
                } catch (IllegalCoordinateException e) {
                    throw new IllegalStateException("Failed to transform " + Arrays.asList(coord) + " from" + fromCrs + " to " + toCrs, e);
                }
        return result;
    }

    private static GeodeticCRS createCrs(String crs) {
        try {
            return (GeodeticCRS) CRS_FACTORY.getCRS(crs);
        } catch (CRSException e) {
            throw new IllegalStateException("Unable to create Coordinate Reference System " + crs, e);
        }
    }

    private static CRSFactory createCrsFactory() {
        CRSFactory crsFactory = new CRSFactory();
        RegistryManager registryManager = crsFactory.getRegistryManager();
        registryManager.addRegistry(new IGNFRegistry());
        registryManager.addRegistry(new EPSGRegistry());
        registryManager.addRegistry(new ESRIRegistry());
        registryManager.addRegistry(new Nad27Registry());
        registryManager.addRegistry(new Nad83Registry());
        registryManager.addRegistry(new worldRegistry());
        return crsFactory;
    }

}
