package ru.brikster.chatty.repository.swear;

import java.util.List;

public interface SwearRepository {

    List<String> getSwears();

    List<String> getWhitelist();

}
