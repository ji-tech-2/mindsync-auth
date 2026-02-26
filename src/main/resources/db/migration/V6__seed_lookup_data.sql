-- Seed data for genders
INSERT INTO genders (gender_name)
VALUES ('Female'), ('Male'), ('Non-binary/Other')
ON CONFLICT (gender_name) DO NOTHING;

-- Seed data for occupations
INSERT INTO occupations (occupation_name)
VALUES ('Employed'), ('Student'), ('Self-employed'), ('Retired'), ('Unemployed')
ON CONFLICT (occupation_name) DO NOTHING;

-- Seed data for work_remotes
INSERT INTO work_remotes (work_rmt_name)
VALUES ('Remote'), ('In-person'), ('Hybrid'), ('Unemployed')
ON CONFLICT (work_rmt_name) DO NOTHING;
