-- V1_0016: Seed Ethiopian banks for settlement account selection
-- All major Ethiopian commercial banks for merchant settlement selection

INSERT INTO inventory.banks (id, code, name, swift, is_active) VALUES
  (gen_random_uuid(), 'AWB', 'Awash Bank', 'AWBHETHAXXX', true),
  (gen_random_uuid(), 'CBE', 'Commercial Bank of Ethiopia', 'CBEETHAA', true),
  (gen_random_uuid(), 'BOA', 'Bank of Abyssinia', 'BOAAETH', true),
  (gen_random_uuid(), 'WB', 'Wegagen Bank', 'WEGAETH', true),
  (gen_random_uuid(), 'UB', 'United Bank', 'UNITETH', true),
  (gen_random_uuid(), 'NIBIB', 'Nib International Bank', 'NIBIBETH', true),
  (gen_random_uuid(), 'CBOE', 'Cooperative Bank of Oromia', 'CBOETH', true),
  (gen_random_uuid(), 'LIB', 'Lion International Bank', 'LIBETHAA', true),
  (gen_random_uuid(), 'ZB', 'Zemen Bank', 'ZEMENETH', true),
  (gen_random_uuid(), 'BB', 'Bunna Bank', 'BUNNAAA', true),
  (gen_random_uuid(), 'ABAY', 'Abay Bank', 'ABAYETH', true),
  (gen_random_uuid(), 'AIB', 'Addis International Bank', 'AIBETHAA', true),
  (gen_random_uuid(), 'OB', 'Oromia Bank', 'OROMIAAA', true),
  (gen_random_uuid(), 'EB', 'Enat Bank', 'ENATETH', true),
  (gen_random_uuid(), 'BRH', 'Berhan Bank', 'BRHANETH', true),
  (gen_random_uuid(), 'HIJRA', 'Hijra Bank', 'HIJRAAA', true),
  (gen_random_uuid(), 'ZZ', 'ZamZam Bank', 'ZAMZAMETH', true),
  (gen_random_uuid(), 'SBL', 'Shabelle Bank', 'SHABETH', true),
  (gen_random_uuid(), 'SNQ', 'Siinqee Bank', 'SINQEEETH', true),
  (gen_random_uuid(), 'SDB', 'Sidama Bank', 'SIDAMAAA', true),
  (gen_random_uuid(), 'AHD', 'Ahadu Bank', 'AHADUETH', true),
  (gen_random_uuid(), 'TSDY', 'Tsehay Bank', 'TSEHAYAA', true),
  (gen_random_uuid(), 'AMAB', 'Amhara Bank', 'AMHARAAA', true),
  (gen_random_uuid(), 'GBE', 'Goh Betoch Bank', 'GOHBETH', true),
  (gen_random_uuid(), 'SKET', 'Siket Bank', 'SIKETETH', true),
  (gen_random_uuid(), 'TSD', 'Tsedey Bank', 'TSEDEYAA', true),
  (gen_random_uuid(), 'GBL', 'Global Bank Ethiopia', 'GBLETHAA', true)
ON CONFLICT (code) DO NOTHING;
