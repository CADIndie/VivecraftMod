package org.vivecraft.client_vr.provider.openxr.control;

import net.minecraft.client.KeyMapping;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.vivecraft.client_vr.provider.control.ActionType;
import org.vivecraft.client_vr.provider.control.ControllerType;
import org.vivecraft.client_vr.provider.control.InputAction;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XRInputAction extends InputAction {

    private final Map<String, List<HandedAction>> handles = new HashMap<>();
    private final List<ChordBinding> chords = new ArrayList<>();
    private boolean chordState;
    private boolean chordChanged;
    public int activeAction;

    public record HandedAction(Long handle, ControllerType hand, ActionType action, String path, int dataIndex) {}

    public final List<DigitalData> digitalData = new ArrayList<>();
    public final List<AnalogData> analogData = new ArrayList<>();

    public XRInputAction(
        KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride)
    {
        super(keyMapping, requirement, type, actionSetOverride);
        this.type = null; //No global type, type is part of the

        for (int c = 0; c < ControllerType.values().length; c++) {
            this.enabled[c] = true;
        }
    }

    public List<HandedAction> getHandle(String controller) {
        return handles.getOrDefault(controller, List.of());
    }

    public void addHandle(String controller, Long handle, ControllerType hand, ActionType actionType, String path) {
        if (!handles.containsKey(controller)) {
            var list = new ArrayList<HandedAction>();
            int dataIndex = this.digitalData.size();
            list.add(new HandedAction(handle, hand, actionType, path, dataIndex));
            handles.put(controller, list);
            this.analogData.add(new AnalogData());
            this.digitalData.add(new DigitalData());
        } else {
            boolean duplicate = handles.get(controller).stream()
                .anyMatch(existing -> existing.handle().equals(handle) && existing.path().equals(path));
            if (!duplicate) {
                int dataIndex = this.digitalData.size();
                handles.get(controller).add(new HandedAction(handle, hand, actionType, path, dataIndex));
                this.analogData.add(new AnalogData());
                this.digitalData.add(new DigitalData());
            } else {
                return;
            }
        }
    }

    public void addChord(List<String> paths) {
        if (!paths.isEmpty() && this.chords.stream()
            .noneMatch(existing -> existing.paths().equals(paths))) {
            this.chords.add(new ChordBinding(List.copyOf(paths)));
        }
    }

    private void updateChordState() {
        if (this.chords.isEmpty()) {
            this.chordState = false;
            this.chordChanged = false;
            return;
        }

        boolean nextState = false;
        for (ChordBinding chord : this.chords) {
            boolean satisfied = true;
            for (String path : chord.paths()) {
                boolean found = false;
                boolean on = false;
                for (DigitalData data : this.digitalData) {
                    if (path.equals(data.path)) {
                        found = true;
                        on = data.state && data.isActive;
                        break;
                    }
                }
                if (!found || !on) {
                    satisfied = false;
                    break;
                }
            }
            if (satisfied) {
                nextState = true;
                break;
            }
        }

        if (nextState != this.chordState) {
            this.chordChanged = true;
        }
        this.chordState = nextState;
    }

    private record ChordBinding(List<String> paths) {}

    private List<DigitalData> digitalData() {
        return this.digitalData;
    }

    private List<AnalogData> analogData() {
        return this.analogData;
    }

    @Nullable
    public HandedAction getActiveAction(String[] controllers) {
        if (controllers == null || this.activeAction < 0) {
            return null;
        }
        if (this.activeAction < this.digitalData().size()) {
            DigitalData activeData = this.digitalData().get(this.activeAction);
            if (activeData.hand != null && activeData.isActive && (activeData.state || activeData.isChanged)) {
                String controller = controllers[activeData.hand.ordinal()];
                for (HandedAction handedAction : this.handles.getOrDefault(controller, List.of())) {
                    if (handedAction.dataIndex() == this.activeAction) {
                        return handedAction;
                    }
                }
            }
        }
        for (int dataIndex = 0; dataIndex < this.digitalData().size(); dataIndex++) {
            DigitalData data = this.digitalData().get(dataIndex);
            if (data.hand == null || !data.isActive || (!data.state && !data.isChanged)) {
                continue;
            }
            String controller = controllers[data.hand.ordinal()];
            for (HandedAction handedAction : this.handles.getOrDefault(controller, List.of())) {
                if (handedAction.dataIndex() == dataIndex) {
                    this.activeAction = dataIndex;
                    return handedAction;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isActive() {
        if (!this.chords.isEmpty()) {
            this.updateChordState();
            if (this.chordState) {
                this.activeAction = 0;
                return true;
            }
            return false;
        }
        for (int i = 0; i < this.digitalData().size(); i++) {
            DigitalData data = this.digitalData().get(i);
            if (data.type == null) {
                continue;
            }
            switch (data.type) {
                case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> {
                    if (this.digitalData().get(i).isActive) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case VEC1, VEC2 -> {
                    if (this.analogData().get(i).isActive) {
                        this.activeAction = i;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isButtonPressed() {
        if (!this.chords.isEmpty()) {
            this.updateChordState();
            return this.chordState;
        }
        for (int i = 0; i < this.digitalData().size(); i++) {
            DigitalData data = this.digitalData().get(i);
            if (data.type == null) {
                continue;
            }
            switch (data.type) {
                case DOUBLE_PRESS -> {
                    if (data.doublePress) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case LONG_PRESS -> {
                    if (data.longPress) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case HOLD -> {
                    if (data.hold) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case TOGGLE -> {
                    if (data.toggle) {
                        this.activeAction = i;
                        return true;
                    }
                }
            }
            if (data.state) {
                this.activeAction = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isButtonChanged() {
        if (!this.chords.isEmpty()) {
            this.updateChordState();
            boolean changed = this.chordChanged;
            this.chordChanged = false;
            return changed;
        }
        for (int i = 0; i < this.digitalData().size(); i++) {
            if (this.digitalData().get(i).isChanged) {
                this.activeAction = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public long getLastOrigin() {
        if (this.activeAction == 0) {
            return 0;
        }
        return digitalData().get(this.activeAction).activeOrigin;
    }

    //Multi-binds are a bit messy
    @Override
    public float getAxis1D(boolean delta) {
        for (int i = 0; i < this.analogData().size(); i++) {
            AnalogData data = this.analogData().get(i);
            if (delta) {
                if (Math.abs(data.deltaX) > 0.0f) {
                    this.activeAction = i;
                    return data.deltaX;
                }
            } else {
                if (Math.abs(data.x) > 0.0f) {
                    this.activeAction = i;
                    return data.x;
                }
            }
        }
        return 0;
    }

    @Override
    public float getAxis1DUseTracked() {
        if (this.currentlyInUse || this.isEnabled()) {
            float axis = this.getAxis1D(false);
            this.currentlyInUse = axis != 0.0F;
            return axis;
        } else {
            return 0.0F;
        }
    }

    //Multi-binds are a bit messy
    @Override
    public Vector2fc getAxis2D(boolean delta) {
        for (int i = 0; i < this.analogData().size(); i++) {
            AnalogData data = this.analogData().get(i);
            if (delta) {
                if (Math.abs(data.deltaX) > 0.0f || Math.abs(data.deltaY) > 0.0f) {
                    this.activeAction = i;
                    return new Vector2f(data.deltaX, data.deltaY);
                }
            } else {
                if (Math.abs(data.x) > 0.0f || Math.abs(data.y) > 0.0f) {
                    this.activeAction = i;
                    return new Vector2f(data.x, data.y);
                }
            }
        }
        return new Vector2f();
    }

    @Override
    public Vector2fc getAxis2DUseTracked() {
        if (this.currentlyInUse || this.isEnabled()) {
            Vector2fc axis = this.getAxis2D(false);
            this.currentlyInUse = axis.x() != 0.0F || axis.y() != 0.0F;
            return axis;
        } else {
            return new Vector2f();
        }
    }

    public static class AnalogData {
        public float x;
        public float y;
        public float deltaX;
        public float deltaY;
        public boolean isChanged;
        public boolean isActive;
        public long activeOrigin;
    }

    public static class DigitalData {
        public boolean state;
        public String path;
        public boolean isChanged;
        public boolean isActive;
        public long lastChange;
        public boolean longPress;
        public boolean toggle;
        public boolean doublePress;
        public boolean hold;
        public long activeOrigin;
        public ActionType type;
        public ControllerType hand;
    }
}
