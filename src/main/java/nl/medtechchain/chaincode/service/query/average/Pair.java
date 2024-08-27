package nl.medtechchain.chaincode.service.query.average;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class Pair<K, V> {
    private final K _1;
    private final V _2;
}
