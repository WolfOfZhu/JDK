/*
 * Copyright (c) 2015, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.graalvm.compiler.lir.aarch64;

import static jdk.vm.ci.code.ValueUtil.asRegister;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.COMPOSITE;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.REG;

import org.graalvm.compiler.asm.aarch64.AArch64Address;
import org.graalvm.compiler.asm.aarch64.AArch64MacroAssembler;
import org.graalvm.compiler.lir.LIRFrameState;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.StandardOp.ImplicitNullCheck;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;

import jdk.vm.ci.code.Register;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.Value;

/**
 * AARCH64 LIR instructions that have one input and one output.
 */
public class AArch64Unary {

    /**
     * Instruction with a {@link AArch64AddressValue memory} operand.
     */
    public static class MemoryOp extends AArch64LIRInstruction implements ImplicitNullCheck {
        public static final LIRInstructionClass<MemoryOp> TYPE = LIRInstructionClass.create(MemoryOp.class);

        private final boolean isSigned;

        @Def({REG}) protected AllocatableValue result;
        @Use({COMPOSITE}) protected AArch64AddressValue input;

        @State protected LIRFrameState state;

        private int targetSize;
        private int srcSize;

        public MemoryOp(boolean isSigned, int targetSize, int srcSize, AllocatableValue result, AArch64AddressValue input, LIRFrameState state) {
            super(TYPE);
            this.targetSize = targetSize;
            this.srcSize = srcSize;
            this.isSigned = isSigned;
            this.result = result;
            this.input = input;
            this.state = state;
        }

        @Override
        public void emitCode(CompilationResultBuilder crb, AArch64MacroAssembler masm) {
            if (state != null) {
                crb.recordImplicitException(masm.position(), state);
            }
            AArch64Address address = input.toAddress();
            Register dst = asRegister(result);
            if (isSigned) {
                masm.ldrs(targetSize, srcSize, dst, address);
            } else {
                masm.ldr(srcSize, dst, address);
            }
        }

        @Override
        public boolean makeNullCheckFor(Value value, LIRFrameState nullCheckState, int implicitNullCheckLimit) {
            int displacement = input.getDisplacement();
            if (state == null && value.equals(input.getBase()) && input.getOffset().equals(Value.ILLEGAL) && displacement >= 0 && displacement < implicitNullCheckLimit) {
                state = nullCheckState;
                return true;
            }
            return false;
        }
    }
}
