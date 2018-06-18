package org.nd4j.linalg.api.ops.impl.accum;

import lombok.val;
import onnx.OnnxProto3;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;

import org.nd4j.imports.descriptors.properties.AttributeAdapter;
import org.nd4j.imports.descriptors.properties.PropertyMapping;
import org.nd4j.imports.descriptors.properties.adapters.BooleanAdapter;
import org.nd4j.imports.graphmapper.tf.TFGraphMapper;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.*;

/**
 * Cumulative sum operation, optionally along dimension.
 *
 * @author Alex Black
 */
public class CumSum extends DynamicCustomOp {

    protected boolean exclusive = false;
    protected boolean reverse = false;

    public CumSum() {
    }


    public CumSum(SameDiff sameDiff, SDVariable x, SDVariable axis) {
        this(sameDiff, x, axis, false, false);
    }

    public CumSum(SameDiff sameDiff, SDVariable x, SDVariable axis, boolean exclusive, boolean reverse) {
        super(null, sameDiff, new SDVariable[]{x, axis});
        this.sameDiff = sameDiff;
        this.exclusive = exclusive;
        this.reverse = reverse;
        addArgs();
    }

    public CumSum(INDArray in, INDArray axis, INDArray result, boolean exclusive, boolean reverse) {
        super(null, new INDArray[]{in, axis}, new INDArray[]{result}, null, (List<Integer>)null);
        this.exclusive = exclusive;
        this.reverse = reverse;
        addArgs();
    }


    @Override
    public String opName() {
        return "cumsum";
    }

    @Override
    public String tensorflowName() {
        return "Cumsum";
    }

    @Override
    public Map<String, Map<String, AttributeAdapter>> attributeAdaptersForFunction() {
        Map<String, Map<String, AttributeAdapter>> ret = new HashMap<>();
        Map<String, AttributeAdapter> tfMappings = new LinkedHashMap<>();

        tfMappings.put("exclusive", new BooleanAdapter());
        tfMappings.put("reverse", new BooleanAdapter());


        ret.put(tensorflowName(), tfMappings);

        return ret;
    }

    @Override
    public Map<String, Map<String, PropertyMapping>> mappingsForFunction() {
        Map<String, Map<String, PropertyMapping>> ret = new HashMap<>();
        Map<String, PropertyMapping> map = new HashMap<>();

        val exclusiveMapper = PropertyMapping.builder()
                .tfAttrName("exclusive")
                .propertyNames(new String[]{"exclusive"})
                .build();

        val reverseMapper = PropertyMapping.builder()
                .tfAttrName("reverse")
                .propertyNames(new String[]{"reverse"})
                .build();


        map.put("exclusive", exclusiveMapper);
        map.put("reverse", reverseMapper);

        ret.put(tensorflowName(), map);

        return ret;
    }

    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {
        TFGraphMapper.getInstance().initFunctionFromProperties(nodeDef.getOp(), this, attributesForNode, nodeDef, graph);
        addArgs();
    }

    protected void addArgs() {
        addIArgument(exclusive ? 1 : 0, reverse ? 1 : 0);
    }

    @Override
    public void initFromOnnx(OnnxProto3.NodeProto node, SameDiff initWith, Map<String, OnnxProto3.AttributeProto> attributesForNode, OnnxProto3.GraphProto graph) {
        super.initFromOnnx(node, initWith, attributesForNode, graph);
    }

    @Override
    public List<SDVariable> doDiff(List<SDVariable> grad) {
        return Collections.singletonList(f().cumsumBp(arg(0), arg(1), grad.get(0), exclusive, reverse));
    }

}
