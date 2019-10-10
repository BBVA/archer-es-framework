package com.bbva.ddd.domain.commands.read;

import com.bbva.common.consumers.record.CRecord;
import com.bbva.ddd.domain.consumers.HandlerContextImpl;

public class CommandHandlerContext extends HandlerContextImpl {

    public CommandHandlerContext(final CRecord consumedRecord) {
        super(new CommandRecord(consumedRecord));
    }

    @Override
    public CommandRecord consumedRecord() {
        return (CommandRecord) consumedRecord;
    }
}
