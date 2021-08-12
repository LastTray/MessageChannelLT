package me.crypnotic.messagechannel.api.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class Pair<T, Y> {
	T key;
	Y value;
}
