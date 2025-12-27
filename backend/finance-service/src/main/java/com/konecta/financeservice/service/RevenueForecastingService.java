package com.konecta.financeservice.service; // Adjust package name to match your project structure

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.konecta.financeservice.dto.ForecastResponseDTO;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Service
public class RevenueForecastingService {

    // --- Scaler Constants (Derived from finance_lstm_scaler.json) ---
    // These match the logic: scaled = (value * scale) + min
    private static final double SCALE_FACTOR = 0.7020179033431557;
    private static final double MIN_OFFSET = -13.819729501558403;

    private final OrtEnvironment env;
    private final OrtSession session;

    public RevenueForecastingService() throws OrtException, IOException {
        // 1. Initialize the ONNX Runtime Environment
        this.env = OrtEnvironment.getEnvironment();

        // 2. Load the Model from src/main/resources/ml/finance_lstm.onnx
        String modelPath = "/ml/finance_lstm.onnx";
        InputStream modelStream = getClass().getResourceAsStream(modelPath);

        if (modelStream == null) {
            throw new IOException("ONNX Model not found at path: " + modelPath);
        }

        byte[] modelBytes = modelStream.readAllBytes();
        this.session = env.createSession(modelBytes, new OrtSession.SessionOptions());
    }

    /**
     * Predicts the revenue for the next quarter based on the previous two quarters.
     *
     * @param lastTwoQuartersRevenue Array of size 2 containing raw revenue (e.g., {350000000.0, 360000000.0})
     * @return The predicted revenue for the upcoming quarter in dollars.
     * @throws OrtException If the ONNX runtime encounters an error.
     */
    public ForecastResponseDTO predictNextQuarter(double[] lastTwoQuartersRevenue) throws OrtException {
        if (lastTwoQuartersRevenue == null || lastTwoQuartersRevenue.length != 2) {
            throw new IllegalArgumentException("Prediction requires exactly 2 quarters of historical revenue.");
        }

        // --- Step 1: Pre-Processing ---
        // Input Shape for Model: [BatchSize=1, Lookback=2, Features=1]
        float[][][] inputData = new float[1][2][1];

        for (int i = 0; i < 2; i++) {
            inputData[0][i][0] = preProcess(lastTwoQuartersRevenue[i]);
        }

        // --- Step 2: Inference (Running the Model) --
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {

            // Map the input tensor to the model's input name
            Map<String, OnnxTensor> inputs = Collections.singletonMap("input", inputTensor);

            // Run the session
            try (OrtSession.Result results = session.run(inputs)) {
                // Extract the result. Model returns [BatchSize=1, Forecast=1]
                float[][] output = (float[][]) results.get(0).getValue();
                float predictedScaledValue = output[0][0];

                // --- Step 3: Post-Processing ---
                return new ForecastResponseDTO(postProcess(predictedScaledValue));
            }
        }
    }

    /**
     * transform: Log(1+x) -> Scale
     */
    private float preProcess(double rawRevenue) {
        // 1. Log Transform: ln(1 + x)
        double logValue = Math.log(1 + rawRevenue);

        // 2. MinMax Scaling: val * scale + min
        return (float) ((logValue * SCALE_FACTOR) + MIN_OFFSET);
    }

    /**
     * inverse_transform: UnScale -> Exp(x)-1
     */
    private double postProcess(float scaledValue) {
        // 1. Inverse Scale
        double logValue = (scaledValue - MIN_OFFSET) / SCALE_FACTOR;

        // 2. Inverse Log: exp(x) - 1
        return Math.exp(logValue) - 1;
    }

    // Cleanup resources when the application shuts down
    @PreDestroy
    public void close() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}