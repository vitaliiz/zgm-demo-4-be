CREATE TABLE word (
    id      INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    word    TEXT NOT NULL UNIQUE
);

CREATE TABLE role (
    id      INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    word_id INTEGER NOT NULL UNIQUE REFERENCES word(id)
);

CREATE TABLE sentence (
    id      INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    text    TEXT NOT NULL UNIQUE
);

CREATE TABLE dialog(
    id                  INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title               TEXT NOT NULL,
    description         TEXT NOT NULL,
    icon                TEXT NOT NULL,
    icon_background     TEXT NOT NULL,
    role_a_id           INTEGER NOT NULL REFERENCES role (id),
    role_b_id           INTEGER NOT NULL REFERENCES role (id)
);

CREATE TABLE dialog_flow_step_sentence (
    dialog_id           INTEGER NOT NULL REFERENCES dialog(id),
    flow_id             INTEGER NOT NULL,
    step                INTEGER NOT NULL,
    word_id             INTEGER NOT NULL REFERENCES word(id),
    sentence_id         INTEGER NOT NULL REFERENCES sentence(id),
    role_id             INTEGER NOT NULL REFERENCES role(id),
    PRIMARY KEY (dialog_id, flow_id, step, sentence_id, role_id)
);

CREATE TABLE translation (
    id          INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    hash_en     VARCHAR(100) NOT NULL,
    lang        CHAR(3) NOT NULL,
    translated  TEXT NOT NULL
);

CREATE TABLE voice (
    id          INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    hash_en     VARCHAR(100) NOT NULL,
    lang        CHAR(3) NOT NULL,
    data        BYTEA NOT NULL
);