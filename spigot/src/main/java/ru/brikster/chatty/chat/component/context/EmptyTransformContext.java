package ru.brikster.chatty.chat.component.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class EmptyTransformContext implements TransformContext {
}
